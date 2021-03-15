package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.event.FundTransferEventType.SAME_ACCOUNT_VALIDATION;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SameAccountValidator implements Validator<FundTransferRequestDTO> {

    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {
        log.info("Validating same-credit-and-debit account for service type [ {} ] ", htmlEscape(request.getFinTxnNo()), htmlEscape(request.getServiceType()));
        if (request.getToAccount().equals(request.getFromAccount())) {
            log.warn("Same Debit and credit account found service type [ {} ] ", htmlEscape(request.getServiceType()));
            auditEventPublisher.publishFailureEvent(SAME_ACCOUNT_VALIDATION, metadata, null,
                    TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME.getCustomErrorCode(), TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME.getErrorMessage(), null);
            return ValidationResult.builder()
                    .success(false)
                    .transferErrorCode(TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME)
                    .build();
        }
        log.info("Same Credit/Debit Account Validating successful service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(SAME_ACCOUNT_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
}
