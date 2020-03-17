package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
@Component
public class CurrencyValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        AccountDetailsDTO fromAccount = context.get("from-account", AccountDetailsDTO.class);
        AccountDetailsDTO toAccount = context.get("to-account", AccountDetailsDTO.class);

        String requestedCurrency = request.getCurrency();
        if (!(requestedCurrency.equals(fromAccount.getCurrency()) || requestedCurrency.equals(toAccount.getCurrency())))
            return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();

        return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH).build();

    }
}
