package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
public class SameAccountValidator implements Validator {
    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {
        if (request.getToAccount().equals(request.getFromAccount()))
            return ValidationResult.builder()
                    .success(false)
                    .transferErrorCode(TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME)
                    .build();

        return ValidationResult.builder().success(true).build();
    }
}
