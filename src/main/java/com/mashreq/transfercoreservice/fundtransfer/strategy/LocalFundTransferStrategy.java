package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static java.lang.Long.valueOf;

/**
 *
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFundTransferStrategy implements FundTransferStrategy {

    private static final String INDIVIDUAL_ACCOUNT = "I";

    private static final int LOCAL_IBAN_LENGTH = 23;
    private static final String LOCAL_PRODUCT_ID = "DBLC";
    public static final String LOCAL_TRANSACTION_CODE = "15";
    private final IBANValidator ibanValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BalanceValidator balanceValidator;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;

    @Value("${app.local.currency}")
    private String localCurrency;

    @Value("${app.uae.address}")
    private String address;


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);


        //TODO Remove the empty qrType
        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(), "", INDIVIDUAL_ACCOUNT);

        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = beneficiaryService.getById((metadata.getPrimaryCif()), valueOf(request.getBeneficiaryId()));
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("to-account-currency", StringUtils.isBlank(beneficiaryDto.getBeneficiaryCurrency())
                ? localCurrency : beneficiaryDto.getBeneficiaryCurrency());
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));


        validationContext.add("iban-length", LOCAL_IBAN_LENGTH);
        responseHandler(ibanValidator.validate(request, metadata, validationContext));

        //Balance Validation
        final BigDecimal transferAmountInSrcCurrency = isCurrencySame(beneficiaryDto, fromAccountDetails)
                ? request.getAmount()
                : getAmountInSrcCurrency(request, beneficiaryDto, fromAccountDetails);
        validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);
        responseHandler(balanceValidator.validate(request, metadata, validationContext));


        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), fromAccountDetails, transferAmountInSrcCurrency);
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount, metadata);

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountDetails, beneficiaryDto);
        log.info("Local Fund transfer initiated.......");

        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata);


        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
                ? transferAmountInSrcCurrency
                : convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
    }

    private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO, final BigDecimal transferAmountInSrcCurrency) {
        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(dealNumber);
        currencyConversionRequestDto.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }

    private boolean isCurrencySame(BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        return sourceAccountDetailsDTO.getCurrency().equalsIgnoreCase(beneficiaryDto.getBeneficiaryCurrency());
    }

    private BigDecimal getAmountInSrcCurrency(FundTransferRequestDTO request, BeneficiaryDto beneficiaryDto, AccountDetailsDTO sourceAccountDetailsDTO) {
        BigDecimal amtToBePaidInSrcCurrency;
        final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
        currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
        currencyRequest.setTransactionCurrency("AED");
        currencyRequest.setTransactionAmount(request.getAmount());
        CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
        amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
        return amtToBePaidInSrcCurrency;
    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
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
                .transactionCode(LOCAL_TRANSACTION_CODE)
                .build();

    }


}
