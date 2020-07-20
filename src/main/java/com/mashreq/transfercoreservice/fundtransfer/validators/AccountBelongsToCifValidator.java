package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_BELONGS_TO_CIF;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBelongsToCifValidator implements Validator {

    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating Account for service type [ {} ] ", htmlEscape(request.getServiceType()));

        final List<AccountDetailsDTO> accounts = context.get("account-details", List.class);
        final Boolean validateToAccount = context.get("validate-to-account", Boolean.class);


        if (validateToAccount != null && validateToAccount && !isAccountNumberBelongsToCif(accounts, request.getToAccount())) {
            auditEventPublisher.publishFailureEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null,
                    ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), null);
            return prepareValidationResult(Boolean.FALSE);
        }

        final Boolean validateFromAccount = context.get("validate-from-account", Boolean.class);

        if (validateFromAccount != null && validateFromAccount && !isAccountNumberBelongsToCif(accounts, request.getFromAccount())) {
            auditEventPublisher.publishFailureEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null,
                    ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), ACCOUNT_NOT_BELONG_TO_CIF.getCustomErrorCode(), null);
            return prepareValidationResult(Boolean.FALSE);
        }


        log.info("Account validation Successful for service type [ {} ] ", htmlEscape(request.getServiceType()));
        auditEventPublisher.publishSuccessEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null);
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
