package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
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
import static org.springframework.web.util.HtmlUtils.htmlEscape;

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
    public static final String GOLD = "XAU";
    public static final String SILVER = "XAG";

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
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

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

        BigDecimal transactionAmount = request.getAmount() == null ? request.getSrcAmount() : request.getAmount();

        CurrencyConversionDto conversionResult = request.getAmount()!=null && !isCurrencySame(toAccount, fromAccount)
                ? getCurrencyExchangeObject(transactionAmount,toAccount,fromAccount)
                : getCurrencyExchangeObject(transactionAmount,fromAccount,toAccount);


        final BigDecimal transferAmountInSrcCurrency = request.getAmount()!=null && !isCurrencySame(toAccount, fromAccount)
            ? conversionResult.getAccountCurrencyAmount()
            : transactionAmount;

        validateAccountContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validateAccountContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccount,transferAmountInSrcCurrency);
        request.setServiceType(getBeneficiaryCode(request));
        if(GOLD.equalsIgnoreCase(request.getCurrency())|| SILVER.equalsIgnoreCase(request.getCurrency())){
            limitValidator.validateMin(userDTO, request.getServiceType(), transactionAmount, metadata);
        }
        final LimitValidatorResponse validationResult = limitValidator.validateWithProc(userDTO, request.getServiceType(), limitUsageAmount, metadata,null);

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccount, toAccount,conversionResult.getExchangeRate(),validationResult);
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata);

        //final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);

        log.info("Total time taken for {} strategy {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid())
                .transactionRefNo(validationResult.getTransactionRefNo())
                .build();

    }

    private String getBeneficiaryCode(FundTransferRequestDTO request) {
        if(GOLD.equalsIgnoreCase(request.getCurrency())){
            return ServiceType.XAU.getName();
        }
        else if(SILVER.equalsIgnoreCase(request.getCurrency())){
            return ServiceType.XAG.getName();
        }
        else return request.getServiceType();
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, AccountDetailsDTO destinationAccount, BigDecimal exchangeRate, LimitValidatorResponse validationResult) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .srcAmount(request.getSrcAmount())
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
                .exchangeRate(exchangeRate)
                .limitTransactionRefNo(validationResult.getTransactionRefNo())
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
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency(LOCAL_CURRENCY);

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private boolean isCurrencySame(AccountDetailsDTO destinationAccount, AccountDetailsDTO sourceAccount) {
        return destinationAccount.getCurrency().equalsIgnoreCase(sourceAccount.getCurrency());
    }

    //convert to sourceAccount from destAccount
    private CurrencyConversionDto getCurrencyExchangeObject(BigDecimal transactionAmount, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccount.getNumber());
        currencyRequest.setAccountCurrency(sourceAccount.getCurrency());
        currencyRequest.setTransactionCurrency(destAccount.getCurrency());
        currencyRequest.setTransactionAmount(transactionAmount);
        return maintenanceService.convertBetweenCurrencies(currencyRequest);

    }

}