package com.mashreq.transfercoreservice.banksearch;

import static com.mashreq.transfercoreservice.common.UAEIbanValidator.validateIban;
import static com.mashreq.transfercoreservice.errors.ExceptionUtils.genericException;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BANK_NOT_FOUND_WITH_IBAN;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SWIFT_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SWIFT_AND_BIC_SEARCH_FAILED;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.OmwCoreClient;
import com.mashreq.transfercoreservice.client.dto.CoreBankDetails;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.MashreqUAEAccountNumberResolver;
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

    private final IbanSearchMWService ibanSearchMWService;
    private final RoutingCodeSearchMWService routingCodeSearchMWService;
    private final IfscCodeSearchMWService ifscCodeSearchMWService;
    private final OmwCoreClient omwClient;
    private final SwiftBankDetailsMapper bankDetailsMapper;
    private final SoapServiceProperties soapServiceProperties;
    private final BICCodeSearchService bicCodeSearchService;
    private final BankRepository bankRepository;
    private final MashreqUAEAccountNumberResolver accountNumberResolver;
    private final static String LOCAL_IBAN_CODE = "AE";
    private final static String MASHREQ_UAE_BANK_CODE = "033";
    
    public BankResultsDto getBankDetails(final String swiftCode, RequestMetaData requestMetadata) {
    	validateSwiftCode(swiftCode);
    	CoreBankDetails response = omwClient.searchAccounts(
    			StringUtils.isNotBlank(swiftCode) && swiftCode.length() == 8 ? swiftCode.concat("XXX") : swiftCode,
    					requestMetadata.getCountry(),
    					soapServiceProperties.getUserId(),
    					requestMetadata.getChannelTraceId());
    	if(ObjectUtils.isEmpty(response)) {
    		GenericExceptionHandler.handleError(INVALID_SWIFT_CODE, INVALID_SWIFT_CODE.getErrorMessage());
    	}
    	return bankDetailsMapper.coreBankResultsToDto(response);
    }

    public List<BankResultsDto> getBankDetails(final String channelTraceId, final BankDetailRequestDto bankDetailRequest, final RequestMetaData requestMetaData) {
        if("bic".equalsIgnoreCase(bankDetailRequest.getType())){
            return bicCodeSearchService.fetchBankDetailsWithBic(bankDetailRequest.getCountryCode(), requestMetaData );
        }
        if ("iban".equals(bankDetailRequest.getType())) {
        	if(isLocalIban(bankDetailRequest.getValue())) {
        		return getLocalIbanBankDetails(bankDetailRequest.getValue());
        	}
            return ibanSearchMWService.fetchBankDetailsWithIban(channelTraceId, bankDetailRequest.getValue(), requestMetaData );
        }
        if("swift".equals(bankDetailRequest.getType())) {
        	validateSwiftCode(bankDetailRequest.getValue());
        	return fetchBySwiftAndBicCode(channelTraceId, bankDetailRequest, requestMetaData);
        }
        return routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest, requestMetaData);
    }

    private List<BankResultsDto> fetchBySwiftAndBicCode(String channelTraceId, BankDetailRequestDto bankDetailRequest, RequestMetaData requestMetaData) {
    	List<BankResultsDto> bankDetails = null;
    	try {
    		bankDetails = routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest, requestMetaData);
    	}
    	catch(GenericException gex) {
    		try {
    			bankDetails = bicCodeSearchService.fetchBankDetailsWithBic(bankDetailRequest.getCountryCode(), requestMetaData );
    		}
    		catch(GenericException ge) {
    			GenericExceptionHandler.handleError(SWIFT_AND_BIC_SEARCH_FAILED, SWIFT_AND_BIC_SEARCH_FAILED.getErrorMessage(), ge.getErrorDetails());
    		}
    	}
		return bankDetails;
	}

	private List<BankResultsDto> getLocalIbanBankDetails(String iban) {
    	validateIban(iban);

        String bankcode = iban.substring(4, 7);
		BankDetails bank = bankRepository.findByBankCode(bankcode).orElseThrow(() -> genericException(BANK_NOT_FOUND_WITH_IBAN));
        
        BankResultsDto bankResults = new BankResultsDto();
        bankResults.setSwiftCode(bank.getSwiftCode());
        bankResults.setBankName(bank.getBankName());
        updateAccountNumber(bankResults,iban,bankcode);
		return Arrays.asList(bankResults);
	}

	private void updateAccountNumber(BankResultsDto bankResults, String iban, String bankcode) {
		if(StringUtils.isNotEmpty(iban) && MASHREQ_UAE_BANK_CODE.equals(bankcode)) {
			bankResults.setAccountNo(accountNumberResolver.generateAccountNumber(iban));
		}
	}

	private boolean isLocalIban(String iban) {
		return Objects.nonNull(iban) && iban.startsWith(LOCAL_IBAN_CODE);
	}

	private void validateSwiftCode(String code) {
		code = StringUtils.trimToEmpty(code);
		if(code.length() != 8 && code.length() != 11) {
			GenericExceptionHandler.handleError(INVALID_SWIFT_CODE, INVALID_SWIFT_CODE.getErrorMessage());
		}
		
	}

	public BankResultsDto getBankDeatilsByIfsc(final String channelTraceId, final String ifscCode, final RequestMetaData requestMetaData) {
        return ifscCodeSearchMWService.getBankDetailByIfscCode(channelTraceId, ifscCode, requestMetaData);
    }

}
