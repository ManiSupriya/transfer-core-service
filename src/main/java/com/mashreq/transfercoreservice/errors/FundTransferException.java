package com.mashreq.transfercoreservice.errors;

/**
 * @author shahbazkh
 * @date 3/15/20
 */
public class FundTransferException extends Exception {

    private final TransferErrorCode transferErrorCode;


    public FundTransferException(TransferErrorCode transferErrorCode, String message) {
        super(message);
        this.transferErrorCode = transferErrorCode;
    }

    public TransferErrorCode getTransferErrorCode() {
        return transferErrorCode;
    }


}
