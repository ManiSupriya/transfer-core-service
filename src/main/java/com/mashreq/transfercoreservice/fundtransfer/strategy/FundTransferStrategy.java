package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

/**
 * @author shahbazkh
 * @date 3/12/20
 */

@FunctionalInterface
public interface FundTransferStrategy {

    void execute(FundTransferRequestDTO request, FundTransferMetadata metadata);

    default void responseHandler(ValidationResult validationResult) {
        if (!validationResult.isSuccess()) {
            GenericExceptionHandler.handleError(validationResult.getTransferErrorCode(), validationResult.getTransferErrorCode().getErrorMessage());
        }
    }

}
