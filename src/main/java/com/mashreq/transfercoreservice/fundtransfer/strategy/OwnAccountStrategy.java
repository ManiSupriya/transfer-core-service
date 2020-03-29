package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;

import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@AllArgsConstructor
@Slf4j
@Service
public class OwnAccountStrategy implements FundTransferStrategy {
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final CurrencyValidator currencyValidator;
    private final LimitValidator limitValidator;
    private AccountService accountService;
    private final CoreTransferService coreTransferService;
    private final MaintenanceService maintenanceService;
    private final BalanceValidator balanceValidator;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", accountsFromCore);
        validateAccountContext.add("validate-to-account", Boolean.TRUE);
        validateAccountContext.add("validate-from-account", Boolean.TRUE);

        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateAccountContext));

        final AccountDetailsDTO toAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getToAccount());
        final AccountDetailsDTO fromAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());

        validateAccountContext.add("from-account", fromAccount);
        validateAccountContext.add("to-account", toAccount);

        responseHandler(currencyValidator.validate(request, metadata, validateAccountContext));

        // As per current implementation with FE they are sending toCurrency and its value for within and own
        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = request.getAmount();
        if(!userDTO.getLocalCurrency().equalsIgnoreCase(request.getCurrency())){

            // Since we support request currency it can be  debitLeg or creditLeg
            String givenAccount = request.getToAccount();
            if(request.getCurrency().equalsIgnoreCase(fromAccount.getCurrency())){
                log.info("Limit Validation with respect to from account.");
                givenAccount = request.getFromAccount();
            }
            CoreCurrencyConversionRequestDto requestDto = generateCurrencyConversionRequest(request.getCurrency(),
                    givenAccount, request.getAmount(),
                    request.getDealNumber(), userDTO.getLocalCurrency());
            CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(requestDto);
            limitUsageAmount = currencyConversionDto.getTransactionAmount();
        }
        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit Validation successful");

        //responseHandler(balanceValidator.validate(request,metadata));

        final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }
}