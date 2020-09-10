package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
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

    FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO);

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
        CoreCurrencyConversionRequestDto coreCurrencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        coreCurrencyConversionRequestDto.setAccountNumber(givenAccount);
        coreCurrencyConversionRequestDto.setAccountCurrency(givenCurrency);
        coreCurrencyConversionRequestDto.setAccountCurrencyAmount(givenAmount);
        coreCurrencyConversionRequestDto.setDealNumber(dealNumber);
        coreCurrencyConversionRequestDto.setTransactionCurrency(localCurrency);
        return coreCurrencyConversionRequestDto ;
    }

}
