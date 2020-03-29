package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/26/20
 */
@Slf4j
@Component
public class BalanceValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        log.info("Validating Balance for service type [ {} ] ", request.getServiceType());
        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);

        log.info("Balance in account [ {} {} ] ", fromAccount.getAvailableBalance(), fromAccount.getCurrency());
        log.info("Amount to be debited  [ {} {} ] ", request.getAmount(), request.getServiceType());

        if (fromAccount.getCurrency().equalsIgnoreCase(request.getCurrency()))
            return isBalanceAvailable(fromAccount.getAvailableBalance(), request.getAmount());

        //TODO Cross currency balance Validation with Limit Management
        return ValidationResult.builder().success(true).build();
    }

    private ValidationResult isBalanceAvailable(BigDecimal availableBalance, BigDecimal paidAmount) {
        return availableBalance.compareTo(paidAmount) >= 0
                ? ValidationResult.builder().success(true).build()
                : ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.BALANCE_NOT_SUFFICIENT).build();
    }
}
