package com.mashreq.transfercoreservice.banksearch;

import static com.mashreq.transfercoreservice.errors.ExceptionUtils.genericException;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_NOT_FOUND_WITH_SWIFT;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_ROUTING_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SWIFT_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SWIFT_AND_BIC_SEARCH_FAILED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mashreq.transfercoreservice.common.ExceptionUtils;
import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import com.mashreq.transfercoreservice.dto.BankResolverRequestDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.mashreq.mobcommons.services.CustomHtmlEscapeUtil;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.OmwCoreClient;
import com.mashreq.transfercoreservice.client.dto.CoreBankDetails;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.repository.BankRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BankDetailService {

    private final RoutingCodeSearchMWService routingCodeSearchMWService;
    private final IfscCodeSearchMWService ifscCodeSearchMWService;
    private final OmwCoreClient omwClient;
    private final SwiftBankDetailsMapper bankDetailsMapper;
    private final SoapServiceProperties soapServiceProperties;
    private final BICCodeSearchService bicCodeSearchService;
    private final BankRepository bankRepository;
    private final MobCommonService mobCommonService;
	private final BankDetailsResolverFactory bankDetailsResolverFactory;
    private static Map<String, String> ALL_COUNTRIES_MAP;
    
    public BankResultsDto getBankDetails(final String swiftCode, RequestMetaData requestMetadata) {
    	validateSwiftCode(swiftCode);
    	CoreBankDetails response = omwClient.searchAccounts(
    			isNotBlank(swiftCode) && swiftCode.length() == 8 ? swiftCode.concat("XXX") : swiftCode,
    					requestMetadata.getCountry(),
    					soapServiceProperties.getUserId(),
    					requestMetadata.getChannelTraceId());
    	if(ObjectUtils.isEmpty(response)) {
    		GenericExceptionHandler.handleError(INVALID_SWIFT_CODE, INVALID_SWIFT_CODE.getErrorMessage());
    	}
    	return bankDetailsMapper.coreBankResultsToDto(response);
    }

	public BankResultsDto getBankDeatilsByIfsc(final String channelTraceId, final String ifscCode, final RequestMetaData requestMetaData) {
        return ifscCodeSearchMWService.getBankDetailByIfscCode(channelTraceId, ifscCode, requestMetaData);
    }

	public List<BankResultsDto> getBankDetails(BankDetailRequestDto bankDetailRequest, RequestMetaData metaData) {
		List<BankResultsDto> results = getBankDetails(metaData.getChannelTraceId(), bankDetailRequest, metaData);
		if ("MT".equals(bankDetailRequest.getJourneyType())) {
			if (Objects.isNull(results) || results.isEmpty()) {
				throw ExceptionUtils.genericException(INVALID_ROUTING_CODE);
			}
			if (null == ALL_COUNTRIES_MAP) {
				ALL_COUNTRIES_MAP = mobCommonService.getCountryCodeMap();
			}
			return results.stream().map(this::modifyBankResult).collect(Collectors.toList());
		}
		return results;
	}

	private List<BankResultsDto> getBankDetails(final String channelTraceId, final BankDetailRequestDto bankDetailRequest, final RequestMetaData requestMetaData) {
		if("bic".equalsIgnoreCase(bankDetailRequest.getType())){
			return bicCodeSearchService.fetchBankDetailsWithBic(bankDetailRequest.getCountryCode(), requestMetaData );
		}
		//if condition for "account" and "iban" can be completely removed. Keeping it for now
		if("account".equals(bankDetailRequest.getType())) {
			BankResolverRequestDto resolverRequest = BankResolverRequestDto.builder()
					.identifier(bankDetailRequest.getValue())
					.journeyType(bankDetailRequest.getJourneyType())
					.bankCode(bankDetailRequest.getBankCode())
					.requestMetaData(requestMetaData)
					.build();
			return bankDetailsResolverFactory.getBankDetailsResolver(bankDetailRequest.getType()).getBankDetails(resolverRequest);
		}
		if ("iban".equals(bankDetailRequest.getType())) {
			BankResolverRequestDto resolverRequest = BankResolverRequestDto.builder()
					.identifier(bankDetailRequest.getValue())
					.journeyType(bankDetailRequest.getJourneyType())
					.requestMetaData(requestMetaData)
					.build();
			return bankDetailsResolverFactory.getBankDetailsResolver(bankDetailRequest.getType()).getBankDetails(resolverRequest);
		}
		if("swift".equals(bankDetailRequest.getType())) {
			validateSwiftCode(bankDetailRequest.getValue());
			if("MT".equals(bankDetailRequest.getJourneyType())) {
				return fetchBySwiftAndBicCode(channelTraceId, bankDetailRequest, requestMetaData);
			}
		}

		return routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest, requestMetaData);
	}

	private void updateBankCode(BankResultsDto bankResult) {
		//update bank code in bank results. This is currently used for PK beneficiary to fetch accountTitle by calling remittanceEnquiry.
		try {
			String swiftCode = bankResult.getSwiftCode();
			if (isNotBlank(swiftCode)) {
				//fetching first 8 character for swift code as bank ms has swift code with 8 and 11 character both
				BankDetails bankDetails = bankRepository.getBankCode(bankResult.getCountryCode(), swiftCode,getModifiedSwiftCode(swiftCode))
						.orElseThrow(() -> genericException(BANK_NOT_FOUND_WITH_SWIFT));
				bankResult.setBankCode(bankDetails.getBankCode());
				bankResult.setBankNameDb(bankDetails.getBankName());
			}
		} catch (Exception ex) {
			log.error("Bank not found with swift code in bank ms", ex);
		}
	}

	/**
	 * @param swiftCode
	 * @return Modified swift code, to compare the value in db
	 * ie. in database some swift codes are 8 digits and some are 11 digits
	 */
	private String getModifiedSwiftCode(String swiftCode) {
		return swiftCode.substring(0, 6) + "%" ;
	}

	private List<BankResultsDto> fetchBySwiftAndBicCode(String channelTraceId, BankDetailRequestDto bankDetailRequest, RequestMetaData requestMetaData) {
		List<BankResultsDto> bankDetails = null;
		try {
			bankDetails = routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest, requestMetaData);
		}
		catch(GenericException gex) {
			try {
				//TODO:have to create separate query for fetching data by swift code.
				bankDetails = bicCodeSearchService.fetchBankDetailsWithBic(bankDetailRequest.getCountryCode(), requestMetaData ).stream()
						.filter(bankResult ->
								isNotBlank(bankResult.getSwiftCode()) &&
										isNotBlank(bankDetailRequest.getValue()) &&
										bankResult.getSwiftCode().equals(bankDetailRequest.getValue()))
						.collect(Collectors.toList());
			}
			catch(GenericException ge) {
				GenericExceptionHandler.handleError(SWIFT_AND_BIC_SEARCH_FAILED, SWIFT_AND_BIC_SEARCH_FAILED.getErrorMessage(), ge.getErrorDetails());
			}
		}

		if(null == bankDetails || bankDetails.isEmpty()) {
			GenericExceptionHandler.handleError(INVALID_SWIFT_CODE, INVALID_SWIFT_CODE.getErrorMessage());
		}
		return bankDetails;
	}





	private void validateSwiftCode(String code) {
		code = StringUtils.trimToEmpty(code);
		if(code.length() != 8 && code.length() != 11) {
			GenericExceptionHandler.handleError(INVALID_SWIFT_CODE, INVALID_SWIFT_CODE.getErrorMessage());
		}
	}

	private BankResultsDto modifyBankResult(BankResultsDto bankResult) {
		updateCountryInBankResults(bankResult);
		updateSwiftCode(bankResult);
		updateBankCode(bankResult);
		return bankResult;
	}

	private void updateCountryInBankResults(BankResultsDto bankResult) {
		if(null != bankResult && null != ALL_COUNTRIES_MAP) {
			if(StringUtils.isBlank(bankResult.getBankCountry()) && StringUtils.isNotBlank(bankResult.getCountryCode())) {
				bankResult.setBankCountry(ALL_COUNTRIES_MAP.get(bankResult.getCountryCode()));
			}
		}
	}

	private void updateSwiftCode(BankResultsDto bankResult) {
		if(null != bankResult && null != bankResult.getSwiftCode()){
			try{
				//reset swift code if it matches routing code
				validateSwiftCode(bankResult.getSwiftCode());
				if(isNotBlank(bankResult.getRoutingCode()) && bankResult.getSwiftCode().equals(bankResult.getRoutingCode())){
					bankResult.setSwiftCode(null);
					bankResult.setRoutingCode(null);
				}
				return;
			}
			catch(GenericException ex){
				log.error("Invalid swift code returned by accuity -> {}" , CustomHtmlEscapeUtil.htmlEscape(bankResult.getSwiftCode()) ,ex);
				//swift is invalid hence reset it
				bankResult.setSwiftCode(null);
			}
		}
	}

}
