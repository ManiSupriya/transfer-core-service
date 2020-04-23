package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.esbcore.bindings.customerservices.mbcdm.remittancepayment.EAIServices;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author shahbazkh
 * @date 4/23/20
 */

@Slf4j
public class QuickRemitResponseHandler {

    private static final String SUCCESS = "S";
    private static final String SUCCESS_CODE_ENDS_WITH = "-000";
    private static final List<String> PROCESSING_STATUS = Arrays.asList("BENEBANKPROCESSING", "BENEBANKREJECTED");

    public  static MwResponseStatus responseHandler(EAIServices response) {
        log.info("Validate response {}", response);
        if (isSuccessFull(response)) {
            return MwResponseStatus.S;
        }

        if (isProcessing(response)) {
            return MwResponseStatus.P;
        }

        log.info("Quick Failed {} , Description: {}",
                response.getBody().getExceptionDetails().getErrorCode(),
                response.getBody().getExceptionDetails().getData());

        return MwResponseStatus.F;
    }

    private static boolean isProcessing(EAIServices response) {
        final String finalTransactionStatus = response.getBody().getRemittancePaymentRes().getFinalTransactionStatus();
        if (SUCCESS.equals(response.getHeader().getStatus()) && PROCESSING_STATUS.contains(finalTransactionStatus)) {
            log.info("Quick Remit Under Process {} , Description: {}",
                    response.getBody().getExceptionDetails().getErrorCode(),
                    response.getBody().getExceptionDetails().getData());
            return true;
        }
        return false;
    }

    private static boolean isSuccessFull(EAIServices response) {
        if ((StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
                && SUCCESS.equals(response.getHeader().getStatus()))) {
            log.info("Quick Remit Successful {} , Description: {}",
                    response.getBody().getExceptionDetails().getErrorCode(),
                    response.getBody().getExceptionDetails().getData());
            return true;
        }
        return false;
    }
}
