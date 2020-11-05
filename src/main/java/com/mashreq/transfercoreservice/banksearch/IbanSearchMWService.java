package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.esbcore.bindings.account.mbcdm.IBANDetailsReqType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.ibandetails.EAIServices;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

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
    private final AsyncUserEventPublisher asyncUserEventPublisher;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";


    public List<BankResultsDto> fetchBankDetailsWithIban(String channelTraceId, String ibanValue, RequestMetaData metaData) {
        log.info("Searching for Bank details with iban [ {} ]", htmlEscape(ibanValue));

        EAIServices response = (EAIServices) webServiceClient.exchange(getIbanEAIRequest(channelTraceId, ibanValue));
        validateOMWResponse(response, metaData, channelTraceId, ibanValue);
        asyncUserEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.IBAN_SEARCH_MW_CALL, metaData, ibanValue, channelTraceId);
        BankResultsDto resultsDto = new BankResultsDto(response.getBody().getIBANDetailsRes());
        return Arrays.asList(resultsDto);

    }

    private void validateOMWResponse(EAIServices response, RequestMetaData metaData, String channelTraceId, String ibanValue) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.IBAN_SEARCH_MW_CALL, metaData, ibanValue, channelTraceId,
                    response.getBody().getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getErrorDescription(),
            response.getBody().getExceptionDetails().getData());
            GenericExceptionHandler.handleError(TransferErrorCode.IBAN_NOT_FOUND,
                    response.getBody().getExceptionDetails().getErrorDescription(), response.getBody().getExceptionDetails().getErrorCode());
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
