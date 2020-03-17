package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

/**
 * @author shahbazkh
 * @date 3/12/20
 */

@FunctionalInterface
public interface FundTransferStrategy {

    // validate fin txn no
    // validate account numbers
    // find digital user
    // find user dto
    // validate beneficiary
    // validate limit

    void execute(FundTransferMetadata metadata, FundTransferRequestDTO request);

    default void responseHandler(ValidationResult validationResult){
        if (!validationResult.isSuccess()) {
            GenericExceptionHandler.handleError(validationResult.getTransferErrorCode(), validationResult.getTransferErrorCode().getErrorMessage());
        }
    }

}
