package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
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
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {
        log.info("Validating same-credit-and-debit account for service type [ {} ] ", request.getFinTxnNo(), request.getServiceType());
        if (request.getToAccount().equals(request.getFromAccount())) {
            log.warn("Same Debit and credit account found service type [ {} ] ", request.getServiceType());
            return ValidationResult.builder()
                    .success(false)
                    .transferErrorCode(TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME)
                    .build();
        }
        log.info("Same Credit/Debit Account Validating successful service type [ {} ] ", request.getServiceType());
        return ValidationResult.builder().success(true).build();
    }
}
