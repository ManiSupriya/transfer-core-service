package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

/**
 * @author shahbazkh
 * @date 5/3/20
 */
@Slf4j
@Service
public class FlexRuleEngineResponseHandler {

    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    Map<String, TransferErrorCode> esbErrorToChannelErrorLookup;

    @PostConstruct
    public void init() {

        esbErrorToChannelErrorLookup = new HashMap<>();
        esbErrorToChannelErrorLookup.put("EAI-FCI-BRK-27441", FLEX_RULE_MIN_TRANSACTION_VIOLATION);
        esbErrorToChannelErrorLookup.put("EAI-FCI-BRK-27438", FLEX_RULE_MAX_TRANSACTION_VIOLATION);
        esbErrorToChannelErrorLookup.put("EAI-FCI-BRK-30071", FLEX_RULE_NO_RATE_PAIRS_PRESENT);
        esbErrorToChannelErrorLookup.put("EAI-FCI-BRK-27439", FLEX_RULE_NO_AGGREGATOR_PRESENT);
        esbErrorToChannelErrorLookup.put("EAI-FCI-BRK-27440", FLEX_RULE_BIC_CODE_NOT_SUPPORTED);

    }

    public void validateResponse(EAIServices response) {
        log.debug("Validate response {}", response);

        final String errorCode = response.getBody().getExceptionDetails().getErrorCode();
        final String headerValue = response.getHeader().getStatus();

        if (isFailure(errorCode, headerValue))
            GenericExceptionHandler.handleError(
                    esbErrorToChannelErrorLookup.getOrDefault(errorCode, FLEX_RULE_ENGINE_FAILED),
                    response.getBody().getExceptionDetails().getErrorDescription(),
                    response.getBody().getExceptionDetails().getErrorCode());
    }

    private static boolean isFailure(String errorCode, String headerValue) {
        return !SUCCESS.equals(headerValue) && !StringUtils.endsWith(errorCode, SUCCESS_CODE_ENDS_WITH);
    }
}
