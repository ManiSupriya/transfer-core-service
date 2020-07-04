package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.event.publisher.AuditEventPublisher;
import com.mashreq.transfercoreservice.event.publisher.Publisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF;
import static com.mashreq.transfercoreservice.event.model.EventType.ACCOUNT_BELONGS_TO_CIF;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBelongsToCifValidator implements Validator {

    private final Publisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating Account for service type [ {} ] ", request.getServiceType());

        final List<AccountDetailsDTO> accounts = context.get("account-details", List.class);
        final Boolean validateToAccount = context.get("validate-to-account", Boolean.class);


        if (validateToAccount != null && validateToAccount && !isAccountNumberBelongsToCif(accounts, request.getToAccount())) {
            auditEventPublisher.publishEvent(ACCOUNT_BELONGS_TO_CIF, EventStatus.FAILURE, metadata, null);
            return prepareValidationResult(Boolean.FALSE);
        }

        final Boolean validateFromAccount = context.get("validate-from-account", Boolean.class);

        if (validateFromAccount != null && validateFromAccount && !isAccountNumberBelongsToCif(accounts, request.getFromAccount())) {
            auditEventPublisher.publishEvent(ACCOUNT_BELONGS_TO_CIF, EventStatus.FAILURE, metadata, null);
            return prepareValidationResult(Boolean.FALSE);
        }


        log.info("Account validation Successful for service type [ {} ] ", request.getServiceType());
        auditEventPublisher.publishEvent(ACCOUNT_BELONGS_TO_CIF, EventStatus.SUCCESS, metadata, null);
        return prepareValidationResult(Boolean.TRUE);
    }

    private boolean isAccountNumberBelongsToCif(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .anyMatch(x -> x.getNumber().equals(accountNumber));
    }

    private ValidationResult prepareValidationResult(boolean status) {
        return status ? ValidationResult.builder().success(true).build() : ValidationResult.builder()
                .success(false)
                .transferErrorCode(ACCOUNT_NOT_BELONG_TO_CIF)
                .build();
    }

}
