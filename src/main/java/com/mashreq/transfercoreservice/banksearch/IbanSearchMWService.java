package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.esbcore.bindings.account.mbcdm.IBANDetailsReqType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.ibandetails.EAIServices;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class IbanSearchMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";


    public BankResultsDto fetchBankDetailsWithIban(String channelTraceId, String ibanValue) {
        log.info("Searching for Bank details with iban [ {} ]", ibanValue);

        EAIServices response = (EAIServices) webServiceClient.exchange(this.getIbanEAIRequest(channelTraceId, ibanValue));
        validateOMWResponse(response);
        BankResultsDto resultsDto = new BankResultsDto(response.getBody().getIBANDetailsRes());
        return resultsDto;

    }

    private void validateOMWResponse(EAIServices response) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {

            GenericExceptionHandler.handleError(TransferErrorCode.IBAN_NOT_FOUND,
                    response.getBody().getExceptionDetails().getErrorDescription());
        }
    }

    public EAIServices getIbanEAIRequest(String channelTranceId, String ibanValue) {
        EAIServices request = new EAIServices();
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getIbanSearch(), channelTranceId));
        request.setBody(new EAIServices.Body());
        IBANDetailsReqType reqType = new IBANDetailsReqType();
        reqType.setIBANNo(ibanValue);
        request.getBody().setIBANDetailsReq(reqType);
        return request;
    }
}
