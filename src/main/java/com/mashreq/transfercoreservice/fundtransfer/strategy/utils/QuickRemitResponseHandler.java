package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import com.mashreq.esbcore.bindings.customerservices.mbcdm.remittancepayment.EAIServices;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 4/23/20
 */

@Slf4j
public class QuickRemitResponseHandler {

    private static final String SUCCESS = "S";
    private static final String RESPONSE_STATUS_PENDING_PROCESSING = "Pending Processing";
    private static final String SUCCESS_CODE = "EAI-RGW-BRK-000";
    private static final String FINAL_TXN_STATUS_PROCESSING = "BENEBANKPROCESSING";
    private static final String FINAL_TXN_STATUS_SUCCESS = "BENEBANKSUCCESS";
    private static final String QR_PAK_SERVICE_CODE = "TRTPTPK";
    public static final String QR_INDIA_SERVICE_CODE = "TRTPTIN";
    public static final String QR_INSTAREM_SERVICE_CODE = "TINSRPMT";

    public static MwResponseStatus responseHandler(EAIServices response) {
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
        final String srvCode = response.getHeader().getSrvCode();
        final String responseStatus = response.getBody().getRemittancePaymentRes().getStatus();
        final String finalTransactionStatus = response.getBody().getRemittancePaymentRes().getFinalTransactionStatus();

        if (QR_INDIA_SERVICE_CODE.equals(srvCode)) {
            final String finalTransactionStatusIndia = response.getBody().getRemittancePaymentRes().getFinalTransactionStatusIndia();
            if (SUCCESS.equals(response.getHeader().getStatus()) && FINAL_TXN_STATUS_PROCESSING.equals(finalTransactionStatusIndia)) {
                log.info("Quick Remit India Under Process {} , Description: {}",
                        response.getBody().getExceptionDetails().getErrorCode(),
                        response.getBody().getExceptionDetails().getData());
                return true;
            }
        }
        if (QR_PAK_SERVICE_CODE.equals(srvCode)) {
            if (SUCCESS.equals(response.getHeader().getStatus()) && RESPONSE_STATUS_PENDING_PROCESSING.equals(responseStatus)) {
                log.info("Quick Remit Pakistan Under Process {} , Description: {}",
                        response.getBody().getExceptionDetails().getErrorCode(),
                        response.getBody().getExceptionDetails().getData());
                return true;
            }
        }
        if (QR_INSTAREM_SERVICE_CODE.equals(srvCode)) {
            if (SUCCESS.equals(response.getHeader().getStatus()) && FINAL_TXN_STATUS_PROCESSING.equals(finalTransactionStatus)) {
                log.info("Quick Remit Instarem Under Process {} , Description: {}",
                        response.getBody().getExceptionDetails().getErrorCode(),
                        response.getBody().getExceptionDetails().getData());
                return true;
            }
        }
        return false;
    }

    private static boolean isSuccessFull(EAIServices response) {
        final String srvCode = response.getHeader().getSrvCode();
        final String responseStatus = response.getBody().getRemittancePaymentRes().getStatus();
        final String finalTransactionStatus = response.getBody().getRemittancePaymentRes().getFinalTransactionStatus();
        final String responseCode = response.getBody().getExceptionDetails().getErrorCode();
        if (QR_INDIA_SERVICE_CODE.equals(srvCode)) {
            final String finalTransactionStatusIndia = response.getBody().getRemittancePaymentRes().getFinalTransactionStatusIndia();
            if ((SUCCESS_CODE.equals(responseCode) && SUCCESS.equals(response.getHeader().getStatus()) && FINAL_TXN_STATUS_SUCCESS.equals(finalTransactionStatusIndia))) {
                log.info("Quick Remit India Successful {} , Description: {}",
                        responseCode, response.getBody().getExceptionDetails().getData());
                return true;
            }
        }
        if (QR_PAK_SERVICE_CODE.equals(srvCode)) {
            if ((SUCCESS_CODE.equals(responseCode) && SUCCESS.equals(response.getHeader().getStatus()) && !RESPONSE_STATUS_PENDING_PROCESSING.equals(responseStatus))) {
                log.info("Quick Remit Pakistan Successful {} , Description: {}",
                        responseCode, response.getBody().getExceptionDetails().getData());
                return true;
            }
        }
        if (QR_INSTAREM_SERVICE_CODE.equals(srvCode)) {
            if ((SUCCESS_CODE.equals(responseCode) && SUCCESS.equals(response.getHeader().getStatus()) && FINAL_TXN_STATUS_SUCCESS.equals(finalTransactionStatus))) {
                log.info("Quick Remit InstaRem Successful {} , Description: {}",
                        responseCode, response.getBody().getExceptionDetails().getData());
                return true;
            }
        }
        log.info("Quick Remit Falied {} , Description: {}",
                responseCode, response.getBody().getExceptionDetails().getData());
        return false;
    }
}
