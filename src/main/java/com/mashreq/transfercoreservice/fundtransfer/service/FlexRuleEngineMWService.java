package com.mashreq.transfercoreservice.fundtransfer.service;


import com.mashreq.esbcore.bindings.customer.mbcdm.FlexRuleEngineReqType;
import com.mashreq.esbcore.bindings.customer.mbcdm.FlexRuleEngineResType;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineRequestDTO;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";

    public FlexRuleEngineResponseDTO getRules(final String channelTraceId, final FlexRuleEngineRequestDTO request) {
        log.info("Flex Rule engine call initiated [ {} ]", request);

        EAIServices response = (EAIServices) webServiceClient.exchange(generateFlexRuleEngineRequest(channelTraceId, request));
        validateOMWResponse(response);

        FlexRuleEngineResType responseDTO = response.getBody().getFlexRuleEngineRes();
        return FlexRuleEngineResponseDTO.builder()
                .chargeAmount(new BigDecimal(responseDTO.getGatewayDetails().get(0).getChargeAmount()))
                .chargeCurrency(responseDTO.getGatewayDetails().get(0).getChargeCurrency())
                .productCode(responseDTO.getGatewayDetails().get(0).getProductCode())
                .build();
    }

    private void validateOMWResponse(EAIServices response) {
        log.debug("Validate response {}", response);
        if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {

            GenericExceptionHandler.handleError(TransferErrorCode.FLEX_RULE_ENGINE_FAILED,
                    response.getBody().getExceptionDetails().getErrorDescription(), response.getBody().getExceptionDetails().getErrorCode());
        }
    }

    private EAIServices generateFlexRuleEngineRequest(String channelTraceId, FlexRuleEngineRequestDTO flexRequest) {
        EAIServices request = new EAIServices();
        request.setBody(new EAIServices.Body());
        request.setHeader(headerFactory.getHeader(soapServiceProperties.getServiceCodes().getFlexRuleEngine(), channelTraceId));

        FlexRuleEngineReqType flexRuleEngineReqType = new FlexRuleEngineReqType();
        flexRuleEngineReqType.setCustAccountNo(flexRequest.getCustomerAccountNo());
        flexRuleEngineReqType.setTransactionAmount(flexRequest.getTransactionAmount().toString());
        flexRuleEngineReqType.setTransactionCurrency(flexRequest.getTransactionCurrency());
        flexRuleEngineReqType.setAccountWithInstitution("XXXXIN");
        flexRuleEngineReqType.setTransactionStatus("STP");
        flexRuleEngineReqType.setValueDate("2020-04-21");
        flexRuleEngineReqType.setTransferType("AC");
        request.getBody().setFlexRuleEngineReq(flexRuleEngineReqType);
        return request;
    }

}
