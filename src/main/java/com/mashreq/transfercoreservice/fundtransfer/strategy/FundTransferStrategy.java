package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author shahbazkh
 * @date 3/12/20
 */

@FunctionalInterface
public interface FundTransferStrategy {

    FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO);

    default void responseHandler(ValidationResult validationResult) {
        if (!validationResult.isSuccess()) {
            GenericExceptionHandler.handleError(validationResult.getTransferErrorCode(), validationResult.getTransferErrorCode().getErrorMessage());
        }
    }

    default AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }

    default CoreCurrencyConversionRequestDto generateCurrencyConversionRequest(
            String givenCurrency, String givenAccount, BigDecimal givenAmount, String dealNumber, String localCurrency) {
        return CoreCurrencyConversionRequestDto.builder()
                .accountNumber(givenAccount)
                .accountCurrency(givenCurrency)
                .accountCurrencyAmount(givenAmount)
                .dealNumber(dealNumber)
                .transactionCurrency(localCurrency)
                .build();
    }

}
