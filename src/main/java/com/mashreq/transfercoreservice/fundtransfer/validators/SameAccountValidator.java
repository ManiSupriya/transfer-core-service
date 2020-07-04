package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.event.publisher.AuditEventPublisher;
import com.mashreq.transfercoreservice.event.publisher.Publisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.event.model.EventType.SAME_ACCOUNT_VALIDATION;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SameAccountValidator implements Validator {

    private final Publisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {
        log.info("Validating same-credit-and-debit account for service type [ {} ] ", request.getFinTxnNo(), request.getServiceType());
        if (request.getToAccount().equals(request.getFromAccount())) {
            log.warn("Same Debit and credit account found service type [ {} ] ", request.getServiceType());
            auditEventPublisher.publishEvent(SAME_ACCOUNT_VALIDATION, EventStatus.FAILURE, metadata, null);
            return ValidationResult.builder()
                    .success(false)
                    .transferErrorCode(TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME)
                    .build();
        }
        log.info("Same Credit/Debit Account Validating successful service type [ {} ] ", request.getServiceType());
        auditEventPublisher.publishEvent(SAME_ACCOUNT_VALIDATION, EventStatus.SUCCESS, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
}
