package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/26/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        log.info("Validating Balance for service type [ {} ] ", request.getServiceType());
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        String toAccountCurrency = context.get("to-account-currency", String.class);

        log.info("Balance in account [ {} {} ] ", fromAccount.getAvailableBalance(), fromAccount.getCurrency());
        log.info("Amount to be credited is [ {} {} ] ", request.getAmount(), toAccountCurrency);

        if (!fromAccount.getCurrency().equalsIgnoreCase(toAccountCurrency)) {
            BigDecimal paidAmountInSourceCurrency = context.get("transfer-amount-in-source-currency", BigDecimal.class);
            log.info("Converted amount {} is {}", fromAccount.getCurrency(), paidAmountInSourceCurrency);
            return isBalanceAvailable(fromAccount.getAvailableBalance(), paidAmountInSourceCurrency);

        }
        return isBalanceAvailable(fromAccount.getAvailableBalance(), request.getAmount());
    }

    private ValidationResult isBalanceAvailable(BigDecimal availableBalance, BigDecimal paidAmount) {
        return availableBalance.compareTo(paidAmount) >= 0
                ? ValidationResult.builder().success(true).build()
                : ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.BALANCE_NOT_SUFFICIENT).build();
    }
}
