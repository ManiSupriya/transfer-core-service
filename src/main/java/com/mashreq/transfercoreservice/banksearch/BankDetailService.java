package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.OmwCoreClient;
import com.mashreq.transfercoreservice.client.dto.CoreBankDetails;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.wsdl.wsdl11.provider.SoapProvider;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_COUNTRY_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SWIFT_CODE;

import java.util.List;

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
        if ("iban".equals(bankDetailRequest.getType())) {
            return ibanSearchMWService.fetchBankDetailsWithIban(channelTraceId, bankDetailRequest.getValue(), requestMetaData );
        }
        if("swift".equals(bankDetailRequest.getType())) {
        	validateSwiftCode(bankDetailRequest.getValue());
        }
        return routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(channelTraceId, bankDetailRequest, requestMetaData);
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
