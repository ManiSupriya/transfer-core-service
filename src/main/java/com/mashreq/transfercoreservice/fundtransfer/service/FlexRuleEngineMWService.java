package com.mashreq.transfercoreservice.fundtransfer.service;


import com.mashreq.esbcore.bindings.customer.mbcdm.FlexRuleEngineReqType;
import com.mashreq.esbcore.bindings.customer.mbcdm.FlexRuleEngineResType;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMWRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMWResponse;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/19/20
 * <p>
 * This class will be used to search bank details based on IFSC code for Quick Remit Journey
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FlexRuleEngineMWService {

    private final WebServiceClient webServiceClient;
    private final HeaderFactory headerFactory;
    private final SoapServiceProperties soapServiceProperties;
    private final FlexRuleEngineResponseHandler responseHandler;
    private final AsyncUserEventPublisher asyncUserEventPublisher;

    public FlexRuleEngineMWResponse getRules(final FlexRuleEngineMWRequest request, final RequestMetaData requestMetaData) {
        log.info("Flex Rule engine call initiated [ {} ]", request);

        EAIServices response = (EAIServices) webServiceClient.exchange(generateFlexRuleEngineRequest(request));
        final String remarks = getRemarks(request, requestMetaData.getPrimaryCif());
        responseHandler.validateResponse(response, requestMetaData, remarks, request.getChannelTraceId());

        asyncUserEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.FLEX_RULE_ENGINE_MW_CALL, requestMetaData, remarks, request.getChannelTraceId());
        FlexRuleEngineResType responseDTO = response.getBody().getFlexRuleEngineRes();

        return FlexRuleEngineMWResponse.builder()
                .productCode(responseDTO.getGatewayDetails().get(0).getProductCode())
                .chargeAmount(responseDTO.getGatewayDetails().get(0).getChargeAmount())
                .chargeCurrency(responseDTO.getGatewayDetails().get(0).getChargeCurrency())
                .accountCurrencyAmount(new BigDecimal(responseDTO.getGatewayDetails().get(0).getAccountCurrencyAmount()))
                .transactionAmount(new BigDecimal(responseDTO.getGatewayDetails().get(0).getTransactionAmount()))
                .exchangeRate(new BigDecimal(responseDTO.getGatewayDetails().get(0).getExchangeRate()))
                .build();
    }

    private String getRemarks(FlexRuleEngineMWRequest request, String cif) {
        return String.format("Cif=%s,customerAccountNo=%s,transactionCurrency=%s,transactionAmount=%s,accountCurrency=%s," +
                        "accountCurrencyAmount=%s,transferType=%s,transactionStatus=%s," +
                        "accountWithInstitution=%s,valueDate=%s,channelTraceId=%s",
                cif,
                request.getCustomerAccountNo(),
                request.getTransactionCurrency(),
                request.getTransactionAmount(),
                request.getAccountCurrency(),
                request.getAccountCurrencyAmount(),
                request.getTransferType(),
                request.getTransactionStatus(),
                request.getAccountWithInstitution(),
                request.getValueDate(),
                request.getChannelTraceId()
        );
    }


    private EAIServices generateFlexRuleEngineRequest(FlexRuleEngineMWRequest flexRequest) {
        EAIServices request = new EAIServices();
        request.setBody(new EAIServices.Body());
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFlexRuleEngine(), flexRequest.getChannelTraceId()));

        FlexRuleEngineReqType flexRuleEngineReqType = new FlexRuleEngineReqType();
        flexRuleEngineReqType.setCustAccountNo(flexRequest.getCustomerAccountNo());


        flexRuleEngineReqType.setTransactionCurrency(flexRequest.getTransactionCurrency());
        flexRuleEngineReqType.setAccountCurrency(flexRequest.getAccountCurrency());

        flexRuleEngineReqType.setAccountCurrencyAmount(flexRequest.getAccountCurrencyAmount());

        flexRuleEngineReqType.setTransactionAmount(flexRequest.getTransactionAmount());


        flexRuleEngineReqType.setAccountWithInstitution(flexRequest.getAccountWithInstitution());
        flexRuleEngineReqType.setTransactionStatus(flexRequest.getTransactionStatus());
        flexRuleEngineReqType.setValueDate(flexRequest.getValueDate());
        flexRuleEngineReqType.setTransferType(flexRequest.getTransferType());

        request.getBody().setFlexRuleEngineReq(flexRuleEngineReqType);
        return request;
    }

}
