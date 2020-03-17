package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.Builder;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 3/17/20
 */
@Data
@Builder
public class ValidationResult {
    private boolean success;
    private TransferErrorCode transferErrorCode;
}
