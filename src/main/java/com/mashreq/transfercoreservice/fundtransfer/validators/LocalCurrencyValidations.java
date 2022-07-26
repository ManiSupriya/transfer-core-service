package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;

@Slf4j
@Component
public class LocalCurrencyValidations {

    private final String localCurrency;
    private final AsyncUserEventPublisher auditEventPublisher;

    public LocalCurrencyValidations(@Value("${app.local.currency}") String localCurrency, AsyncUserEventPublisher auditEventPublisher) {
        this.localCurrency = localCurrency;
        this.auditEventPublisher = auditEventPublisher;
    }

    public ValidationResult performLocalCurrencyChecks(LocalCurrencyValidationRequest request) {
        log.info("Local account and currency validation for service type [{}] and transaction currency [{}]",
                htmlEscape(request.getServiceType()), htmlEscape(request.getTransactionCurrency()));
        AccountDetailsDTO fromAccount = request.getValidationContext().get("from-account", AccountDetailsDTO.class);
        //String requestedCurrency = request.getCurrency();
        String requestedCurrency = request.getTransactionCurrency();

        // WYMA, WAMA & LOCAL If source account is EGP, transaction should be in EGP currency
        if (isWithinRegionTransfer(request) && localCurrency.equals(fromAccount.getCurrency())) {
            if (!localCurrency.equals(requestedCurrency)) {
                log.error("Transaction currency [{}] not allowed. Transfer is allowed only in local currency [{}] for transation type [{}]",
                        requestedCurrency, localCurrency, request.getServiceType());
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }
        }

        // WYMA - If source account is EGP, destination should be EGP account
        if (WYMA.getName().equals(request.getServiceType()) && localCurrency.equals(fromAccount.getCurrency())) {
            AccountDetailsDTO toAccount = request.getValidationContext().get("to-account", AccountDetailsDTO.class);
            if (!localCurrency.equals(toAccount.getCurrency())) {
                log.error("Destination account currency [{}] not allowed. For transation type [{}], destination account should be in local currency [{}]",
                        toAccount.getCurrency(), request.getServiceType(), localCurrency);
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(),
                        null);
                return ValidationResult.builder().success(false).transferErrorCode(ACCOUNT_CURRENCY_MISMATCH).build();
            }
        }

        // WAMA - If source account is EGP, destination should be EGP account
        if (WAMA.getName().equals(request.getServiceType()) && localCurrency.equals(fromAccount.getCurrency())) {
            SearchAccountDto toAccount = request.getValidationContext().get("credit-account-details", SearchAccountDto.class);
            if (!localCurrency.equals(toAccount.getCurrency())) {
                log.error("Destination account currency [{}] not allowed. For transation type [{}], destination account should be in local currency [{}]",
                        toAccount.getCurrency(), request.getServiceType(), localCurrency);
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(),
                        null);
                return ValidationResult.builder().success(false).transferErrorCode(ACCOUNT_CURRENCY_MISMATCH).build();
            }
        }

        // INFT - Source account cannot be EGP
        // INFT - Transaction currency cannot be EGP
        if (INFT.getName().equals(request.getServiceType())) {
            if (localCurrency.equals(requestedCurrency)) {
                log.error("For transation type [{}], transaction cannot be in local currency [{}]",
                        request.getServiceType(), localCurrency);
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        CURRENCY_IS_INVALID.getCustomErrorCode(), CURRENCY_IS_INVALID.getErrorMessage(), null);
                return ValidationResult.builder().success(false).transferErrorCode(CURRENCY_IS_INVALID).build();
            }

            if (localCurrency.equals(fromAccount.getCurrency())) {
                log.error("For transation type [{}], source account cannot be a local currency [{}] account",
                        request.getServiceType(), localCurrency);
                auditEventPublisher.publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, request.getRequestMetaData(), null,
                        ACCOUNT_CURRENCY_MISMATCH.getCustomErrorCode(), ACCOUNT_CURRENCY_MISMATCH.getErrorMessage(),
                        null);
                return ValidationResult.builder().success(false).transferErrorCode(ACCOUNT_CURRENCY_MISMATCH).build();
            }

        }
        return null;
    }

    private boolean isWithinRegionTransfer(LocalCurrencyValidationRequest request) {
        return WAMA.getName().equals(request.getServiceType()) || WYMA.getName().equals(request.getServiceType())
                || LOCAL.getName().equals(request.getServiceType());
    }



    @Data
    @Builder
    public static class LocalCurrencyValidationRequest {

        private String serviceType;
        private String transactionCurrency;
        private ValidationContext validationContext;
        private RequestMetaData requestMetaData;
    }
}
