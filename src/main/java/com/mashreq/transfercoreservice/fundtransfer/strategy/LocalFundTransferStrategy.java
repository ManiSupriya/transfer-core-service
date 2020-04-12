package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.Long.valueOf;
import static java.time.Instant.now;

/**
 *
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class LocalFundTransferStrategy implements FundTransferStrategy {

    private static final String LOCAL = "LOCAL";
    private static final int LOCAL_IBAN_LENGTH = 23;
    private static final String LOCAL_PRODUCT_ID = "DBLC";
    private final IBANValidator ibanValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;
    private final MaintenanceClient maintenanceClient;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BalanceValidator balanceValidator;
    private final MaintenanceService maintenanceService;

    @Value("${app.local.currency}")
    private String localCurrency;

    @Value("${app.uae.address}")
    private String address;

   /* @Value("${app.local.currency}")
    private String localCurrency;*/


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);


        final Set<PurposeOfTransferDto> allPurposeCodes = maintenanceClient.getAllPurposeCodes(LOCAL).getData();
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        log.info("Purpose code and description validation successful");


        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));
        log.info("Account belongs to cif validation successful");
        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById((metadata.getPrimaryCif()),valueOf(request.getBeneficiaryId()));
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", StringUtils.isBlank(beneficiaryDto.getBeneficiaryCurrency())
                ? localCurrency : beneficiaryDto.getBeneficiaryCurrency());
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        log.info("Beneficiary validation successful");

        validationContext.add("iban-length",LOCAL_IBAN_LENGTH);
        responseHandler(ibanValidator.validate(request, metadata, validationContext));
        log.info("IBAN validation successful");

        BigDecimal sourceAcctCurrencyAmt = null;
        if (!fromAccountDetails.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency())) {
            final CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                    .accountNumber(fromAccountDetails.getNumber())
                    .accountCurrency(fromAccountDetails.getCurrency())
                    .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                    .transactionAmount(request.getAmount()).build();
            CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceClient.convertBetweenCurrencies(currencyRequest).getData();
            sourceAcctCurrencyAmt = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
            validationContext.add("transfer-amount-in-source-currency",conversionResultInSourceAcctCurrency.getAccountCurrencyAmount());
        }

        responseHandler(balanceValidator.validate(request, metadata, validationContext));
        log.info("Balance validation successful");

        log.info("Limit Validation start.");
        // Assuming to account is always in local currency so on currency conversion required
        BigDecimal limitUsageAmount = request.getAmount();
        if (!userDTO.getLocalCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency())) {
            // Since we support request currency it can be  debitLeg or creditLeg
            CoreCurrencyConversionRequestDto requestDto;
            if(Objects.nonNull(sourceAcctCurrencyAmt)) {
                requestDto = generateCurrencyConversionRequest(fromAccountDetails.getCurrency(),
                        fromAccountDetails.getNumber(), sourceAcctCurrencyAmt,
                        request.getDealNumber(), userDTO.getLocalCurrency());
            }
            else {
                requestDto = generateCurrencyConversionRequest(beneficiaryDto.getBeneficiaryCurrency(),
                        beneficiaryDto.getAccountNumber(), request.getAmount(),
                        request.getDealNumber(), userDTO.getLocalCurrency());
            }
            CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(requestDto);
            limitUsageAmount = currencyConversionDto.getTransactionAmount();
        }

        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountDetails, beneficiaryDto);
        log.info("Local Fund transfer initiated.......");

        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        return FundTransferRequest.builder()
                .productId(LOCAL_PRODUCT_ID)
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
                .destinationCurrency(localCurrency)
                .awInstName(beneficiaryDto.getBankName())
                .awInstBICCode(beneficiaryDto.getSwiftCode())
                .beneficiaryAddressTwo(address)
                .build();

    }


    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }


}
