package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.esbcore.bindings.customer.mbcdm.FetchAccuityDataReqType;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.fetchaccuitydata.EAIServices;
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

import java.util.List;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_COUNTRY_CODE;
import static org.apache.commons.lang3.StringUtils.upperCase;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingCodeSearchMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private final AsyncUserEventPublisher asyncUserEventPublisher;
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";

    public List<BankResultsDto> fetchBankDetailsWithRoutingCode(String channelTraceId, BankDetailRequestDto bankDetailRequest, RequestMetaData requestMetaData) {

        log.info("Searching for Bank details with routing code ", bankDetailRequest);

        EAIServices response = (EAIServices) webServiceClient.exchange(
                this.getRequestForRoutingCode(channelTraceId, bankDetailRequest));

        validateOMWResponse(response, requestMetaData, channelTraceId, bankDetailRequest);
        asyncUserEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.ROUTING_CODE_SEARCH_MW_CALL, requestMetaData, bankDetailRequest.toString(), channelTraceId);
        List<BankResultsDto> results = response.getBody().getFetchAccuityDataRes().getAccuityDetails()
                .stream()
                .map(x -> new BankResultsDto(x))
                .collect(Collectors.toList());

        return results;
    }


    private void validateOMWResponse(EAIServices response,RequestMetaData metaData, String channelTraceId, BankDetailRequestDto bankDetailRequest) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.ROUTING_CODE_SEARCH_MW_CALL, metaData, bankDetailRequest.toString(), channelTraceId,
                    response.getBody().getExceptionDetails().getErrorCode(), response.getBody().getExceptionDetails().getErrorDescription(),
                    response.getBody().getExceptionDetails().getData());
            GenericExceptionHandler.handleError(TransferErrorCode.ROUTING_CODE_NOT_FOUND,
                    response.getBody().getExceptionDetails().getErrorDescription(), response.getBody().getExceptionDetails().getErrorCode());
        }
    }

    private EAIServices getRequestForRoutingCode(String channelTraceId, BankDetailRequestDto bankDetailRequest) {
        EAIServices request = new EAIServices();
        request.setBody(new EAIServices.Body());
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getRoutingCodeSearch(), channelTraceId));
        FetchAccuityDataReqType reqBody = new FetchAccuityDataReqType();
        reqBody.setCountryCode(bankDetailRequest.getCountryCode());
        switch (bankDetailRequest.getType()) {
            case "swift":
                reqBody.setRoutingType(StringUtils.upperCase(bankDetailRequest.getType()));
                reqBody.setRoutingCode(upperCase(bankDetailRequest.getValue()));
                break;
            case "routing-code":
                reqBody.setRoutingType(StringUtils.upperCase(getRoutingCodeType(bankDetailRequest)));
                reqBody.setRoutingCode(StringUtils.upperCase(bankDetailRequest.getValue()));
                break;
            default:
                break;
        }
        request.getBody().setFetchAccuityDataReq(reqBody);
        return request;
    }

    private String getRoutingCodeType(BankDetailRequestDto bankDetailRequest) {
        final String routingTypeCode = bankDetailRequest.getRoutingCodeType();
        RoutingCodeType routingCodeType = RoutingCodeType.getRoutingCodeByType(routingTypeCode);
        if (!routingCodeType.name().equals(bankDetailRequest.getCountryCode())) {
            GenericExceptionHandler.handleError(INVALID_COUNTRY_CODE, INVALID_COUNTRY_CODE.getErrorMessage());
        }

        return routingTypeCode;
    }
}
