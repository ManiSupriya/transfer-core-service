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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    @Value("${app.local.currency}")
    private String localCurrency;

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
        log.info("Beneficiary validation successful");

        final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("to-account-currency",beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);
        //validation of swift, iban and routing code is taken care during adding beneficiary, so not validating here
        BigDecimal sourceAcctCurrencyAmt = request.getAmount();
        if (!sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency())) {
            final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                    .accountNumber(sourceAccountDetailsDTO.getNumber())
                    .accountCurrency(sourceAccountDetailsDTO.getCurrency())
                    .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                    .transactionAmount(request.getAmount()).build();
            CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceClient.convertBetweenCurrencies(currencyRequest).getData();
            sourceAcctCurrencyAmt = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
            validationContext.add("transfer-amount-in-source-currency",conversionResultInSourceAcctCurrency.getAccountCurrencyAmount());
        }

        responseHandler(balanceValidator.validate(request, metadata, validationContext));
        log.info("Balance validation successful");

        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = sourceAcctCurrencyAmt;
        if (!localCurrency.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())) {

            CoreCurrencyConversionRequestDto requestDto = generateCurrencyConversionRequest(sourceAccountDetailsDTO.getCurrency(),
                        sourceAccountDetailsDTO.getNumber(), limitUsageAmount,
                        request.getDealNumber(), localCurrency);

            CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(requestDto);
            limitUsageAmount = currencyConversionDto.getTransactionAmount();
        }
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, sourceAccountDetailsDTO, beneficiaryDto);
        log.info("International Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        final FundTransferRequest req = FundTransferRequest.builder()
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

        if (isRoutingCodeCountry(beneficiaryDto.getRoutingCode())) {
            return req.toBuilder()
                    .awInstBICCode(ROUTING_CODE_PREFIX + beneficiaryDto.getRoutingCode())
                    .awInstName(beneficiaryDto.getSwiftCode())
                    .build();
        } else {
            return req.toBuilder()
                    .awInstBICCode(beneficiaryDto.getSwiftCode())
                    .awInstName(beneficiaryDto.getBankName())
                    .build();
        }
    }

    private boolean isRoutingCodeCountry(String routingCode) {
        return StringUtils.isNotBlank(routingCode);
    }

    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }

}
