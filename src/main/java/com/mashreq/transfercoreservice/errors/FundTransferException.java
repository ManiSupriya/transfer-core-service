package com.mashreq.transfercoreservice.errors;

/**
 * @author shahbazkh
 * @date 3/15/20
 */
public class FundTransferException extends Exception {

    private final TransferErrorCode transferErrorCode;
    private final String externalErrorCode;


    public FundTransferException(TransferErrorCode transferErrorCode, String message, String externalErrorCode) {
        super(message);
        this.transferErrorCode = transferErrorCode;
        this.externalErrorCode = externalErrorCode;
    }

    public TransferErrorCode getTransferErrorCode() {
        return transferErrorCode;
    }


}
