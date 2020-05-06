package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static java.time.Duration.between;
import static java.time.Instant.now;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnAccountStrategy implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";
    public static final String OWN_ACCOUNT_TRANSACTION_CODE = "096";
    public static final String LOCAL_CURRENCY = "AED";

    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final CurrencyValidator currencyValidator;
    private final LimitValidator limitValidator;
    private final AccountService accountService;
    private final MaintenanceService maintenanceService;
    private final FundTransferMWService fundTransferMWService;
    private final BalanceValidator balanceValidator;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {

        Instant start = Instant.now();

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
        validateAccountContext.add("to-account-currency", toAccount.getCurrency());
        responseHandler(currencyValidator.validate(request, metadata, validateAccountContext));


        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(toAccount, fromAccount)
                ? request.getAmount()
                : getAmountInSrcCurrency(request, toAccount, fromAccount);
        validateAccountContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validateAccountContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccount,transferAmountInSrcCurrency);
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);



        // As per current implementation with FE they are sending toCurrency and its value for within and own
//        log.info("Limit Validation start.");
//        BigDecimal limitUsageAmount = request.getAmount();
//        if (!userDTO.getLocalCurrency().equalsIgnoreCase(request.getCurrency())) {
//
//            // Since we support request currency it can be  debitLeg or creditLeg
//            String givenAccount = request.getToAccount();
//            if (request.getCurrency().equalsIgnoreCase(fromAccount.getCurrency())) {
//                log.info("Limit Validation with respect to from account.");
//                givenAccount = request.getFromAccount();
//            }
//            CoreCurrencyConversionRequestDto requestDto = generateCurrencyConversionRequest(request.getCurrency(),
//                    givenAccount, request.getAmount(),
//                    request.getDealNumber(), userDTO.getLocalCurrency());
//            CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(requestDto);
//            limitUsageAmount = currencyConversionDto.getTransactionAmount();
//        }
//        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
//        log.info("Limit Validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccount, toAccount);
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest);

        //final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);

        log.info("Total time taken for {} strategy {} milli seconds ", request.getServiceType(), between(start, now()).toMillis());

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, AccountDetailsDTO destinationAccount) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(destinationAccount.getNumber())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(sourceAccount.getCurrency())
                .sourceBranchCode(sourceAccount.getBranchCode())
                .beneficiaryFullName(destinationAccount.getCustomerName())
                .destinationCurrency(destinationAccount.getCurrency())
                .transactionCode(OWN_ACCOUNT_TRANSACTION_CODE)
                .internalAccFlag(INTERNAL_ACCOUNT_FLAG)
                .build();

    }


    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return LOCAL_CURRENCY.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                                    final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .accountCurrencyAmount(transferAmountInSrcCurrency)
                .dealNumber(dealNumber)
                .transactionCurrency(LOCAL_CURRENCY)
                .build();

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private boolean isCurrencySame(AccountDetailsDTO destinationAccount, AccountDetailsDTO sourceAccount) {
        return destinationAccount.getCurrency().equalsIgnoreCase(sourceAccount.getCurrency());
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
        final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccount.getNumber())
                .accountCurrency(sourceAccount.getCurrency())
                .transactionCurrency(destAccount.getCurrency())
                .transactionAmount(request.getAmount())
                .build();
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        BigDecimal amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

}