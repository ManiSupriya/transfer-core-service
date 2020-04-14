package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;


import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

import static java.lang.Long.valueOf;

/**
 *
 */
@AllArgsConstructor
@Slf4j
@Service
public class InternationalFundTransferStrategy implements FundTransferStrategy {

    private static final String INTERNATIONAL = "INTERNATIONAL";
    private static final String INTERNATIONAL_PRODUCT_ID = "DBFC";
    private static final String ROUTING_CODE_PREFIX = "//";
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountService accountService;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final MaintenanceClient maintenanceClient;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final FundTransferMWService fundTransferMWService;
    private final MaintenanceService maintenanceService;

    private final BeneficiaryClient beneficiaryClient;
    private final LimitValidator limitValidator;

    private final HashMap<String, String> countryToCurrencyMap = new HashMap<>();

    //Todo: Replace with native currency fetched from API call
    @PostConstruct
    private void initCountryToNativeCurrencyMap() {
        countryToCurrencyMap.put("IN", "INR");
        countryToCurrencyMap.put("AU", "AUD");
        countryToCurrencyMap.put("CA", "CAD");
        countryToCurrencyMap.put("NZ", "NZD");
        countryToCurrencyMap.put("UK", "GBP");
        countryToCurrencyMap.put("US", "USD");
    }

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final Set<PurposeOfTransferDto> allPurposeCodes = maintenanceClient.getAllPurposeCodes(INTERNATIONAL).getData();
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        log.info("Purpose code and description validation successful");

        final BeneficiaryDto beneficiaryDto = beneficiaryClient.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()))
                .getData();
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));


        final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);
        if (!sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency())) {
            validationContext.add("transfer-amount-in-source-currency", getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO));
        }
        responseHandler(balanceValidator.validate(request, metadata, validationContext));
        log.info("Balance validation successful");


        BigDecimal limitUsageAmount = getLimitUsageAmount(request, sourceAccountDetailsDTO);
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, sourceAccountDetailsDTO, beneficiaryDto);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private BigDecimal getAmountInLocalCurrency(FundTransferRequestDTO request, AccountDetailsDTO sourceAccountDetailsDTO, BigDecimal limitUsageAmount) {
        CoreCurrencyConversionRequestDto requestDto = generateCurrencyConversionRequest(sourceAccountDetailsDTO.getCurrency(),
                sourceAccountDetailsDTO.getNumber(), limitUsageAmount,
                request.getDealNumber(), "AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(requestDto);
        limitUsageAmount = currencyConversionDto.getTransactionAmount();
        return limitUsageAmount;
    }

    private BigDecimal getLimitUsageAmount(FundTransferRequestDTO request, AccountDetailsDTO sourceAccountDetailsDTO) {
        return "AED" .equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? request.getAmount()
                : getAmountInLocalCurrency(request, sourceAccountDetailsDTO, request.getAmount());
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(sourceAccountDetailsDTO.getNumber())
                .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .transactionAmount(request.getAmount()).build();
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceClient.convertBetweenCurrencies(currencyRequest).getData();
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        final FundTransferRequest fundTransferRequest = FundTransferRequest.builder()
                .productId(INTERNATIONAL_PRODUCT_ID)
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
                .purposeCode(request.getPurposeCode())
                .purposeDesc(request.getPurposeDesc())
                .chargeBearer(request.getChargeBearer())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(accountDetails.getCurrency())
                .sourceBranchCode(accountDetails.getBranchCode())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .destinationCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .beneficiaryAddressOne(beneficiaryDto.getAddressLine1())
                .beneficiaryAddressTwo(beneficiaryDto.getAddressLine2())
                .beneficiaryAddressThree(beneficiaryDto.getAddressLine3())
                .build();

        return enrichFundTransferRequestByCountryCode(fundTransferRequest, beneficiaryDto);
    }

    private FundTransferRequest enrichFundTransferRequestByCountryCode(FundTransferRequest request, BeneficiaryDto beneficiaryDto) {
        List<CountryMasterDto> countryList = maintenanceClient.getAllCountries("MOB", "AE", Boolean.TRUE).getData();
        final Optional<CountryMasterDto> countryDto = countryList.stream()
                .filter(country -> country.getCode().equals(beneficiaryDto.getBeneficiaryCountryISO()))
                .findAny();
        if (countryDto.isPresent()) {
            final CountryMasterDto countryMasterDto = countryDto.get();
            if (StringUtils.isNotBlank(countryMasterDto.getRoutingCode()) && request.getDestinationCurrency()
                    .equals(countryToCurrencyMap.get(beneficiaryDto.getBeneficiaryCountryISO()))) {

                return request.toBuilder()
                        .awInstBICCode(ROUTING_CODE_PREFIX + beneficiaryDto.getRoutingCode())
                        .awInstName(beneficiaryDto.getSwiftCode())
                        .build();
            }
        }
        return request.toBuilder()
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .awInstName(beneficiaryDto.getBankName())
                .build();
    }


    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }

}
