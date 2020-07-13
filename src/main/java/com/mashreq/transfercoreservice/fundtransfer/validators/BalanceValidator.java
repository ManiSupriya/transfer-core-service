package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BALANCE_NOT_SUFFICIENT;

/**
 * @author shahbazkh
 * @date 3/26/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceValidator implements Validator {


    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating Balance for service type [ {} ] ", request.getServiceType());
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        log.info("Balance in account [ {} {} ] ", fromAccount.getAvailableBalance(), fromAccount.getCurrency());

        BigDecimal transferAmountInSrcCurrency = context.get("transfer-amount-in-source-currency", BigDecimal.class);
        log.info("Amount to be credited is [ {} {} ] ", transferAmountInSrcCurrency, fromAccount.getCurrency());

        if (!isBalanceAvailable(fromAccount.getAvailableBalance(), transferAmountInSrcCurrency)) {
            auditEventPublisher.publishFailureEvent(FundTransferEventType.BALANCE_VALIDATION, metadata, null,
                    BALANCE_NOT_SUFFICIENT.getErrorMessage(),BALANCE_NOT_SUFFICIENT.getCustomErrorCode(), null);
            return ValidationResult.builder().success(false).transferErrorCode(BALANCE_NOT_SUFFICIENT)
                    .build();
        }
        log.info("Balance Validation successful");
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.BALANCE_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }

    private boolean isBalanceAvailable(BigDecimal availableBalance, BigDecimal paidAmount) {
        return availableBalance.compareTo(paidAmount) >= 0;
    }
}
