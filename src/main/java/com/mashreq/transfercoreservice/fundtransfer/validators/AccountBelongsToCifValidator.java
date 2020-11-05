package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_NOT_BELONG_TO_CIF;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_BELONGS_TO_CIF;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.ACCOUNT_IS_DORMENT;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_IS_IN_DORMENT;
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
    private static final String ACCOUNT_DORMANT = "DORMANT";

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

        if (validateFromAccount != null && validateFromAccount  && !isAccountNumberBelongsToCif(accounts, request.getFromAccount())) {
            auditEventPublisher.publishFailureEvent(ACCOUNT_BELONGS_TO_CIF, metadata, null,
                    ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage(), ACCOUNT_NOT_BELONG_TO_CIF.getCustomErrorCode(), null);
            return prepareValidationResult(Boolean.FALSE);
        }

        if(validateFromAccount != null && isAccountNotDormant(accounts, request.getFromAccount())){
            auditEventPublisher.publishFailureEvent(ACCOUNT_IS_DORMENT, metadata, null,
                    ACCOUNT_IS_IN_DORMENT.getErrorMessage(), ACCOUNT_IS_IN_DORMENT.getCustomErrorCode(), null);
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

    private boolean isAccountNotDormant(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream().anyMatch(x -> x.getNumber().equals(accountNumber) && StringUtils.isNotBlank(x.getStatus()) && x.getStatus().equalsIgnoreCase(ACCOUNT_DORMANT));

    }

    private ValidationResult prepareValidationResult(boolean status) {
        return status ? ValidationResult.builder().success(true).build() : ValidationResult.builder()
                .success(false)
                .transferErrorCode(ACCOUNT_NOT_BELONG_TO_CIF)
                .build();
    }

}
