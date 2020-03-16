package com.mashreq.transfercoreservice.errors;

import java.util.stream.Stream;

/**
 * @author shahbazkh
 * @date 3/15/20
 */
public class FundTransferException extends Exception {

    private TransferErrorCode transferErrorCode;


    public FundTransferException(TransferErrorCode transferErrorCode, String message) {
        super(message);
        this.transferErrorCode = transferErrorCode;
    }

    public TransferErrorCode getTransferErrorCode() {
        return transferErrorCode;
    }


}
