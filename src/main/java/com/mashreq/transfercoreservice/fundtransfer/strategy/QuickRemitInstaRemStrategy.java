package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferContext.Constants;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.mapper.QuickRemitInstaRemRequestMapper;
import com.mashreq.transfercoreservice.fundtransfer.service.FlexRuleEngineService;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.Long.valueOf;

/**
 * @author shahbazkh
 * @date 5/5/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickRemitInstaRemStrategy implements QuickRemitFundTransfer {

    public static final String INDIVIDUAL_TYPE = "I";
    private final QuickRemitInstaRemRequestMapper mapper;
    private final QuickRemitFundTransferMWService quickRemitFundTransferMWService;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountService accountService;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final BalanceValidator balanceValidator;
    private final MaintenanceService maintenanceService;
    private final MobCommonService mobCommonService;
    private final LimitValidator limitValidator;
    private final FlexRuleEngineService flexRuleEngineService;


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO, ValidationContext validationContext) {

        log.info("Quick Remit InstaRem initiated");

        responseHandler(finTxnNoValidator.validate(request, metadata));
        final CustomerDetailsDto customerDetails = mobCommonService.getCustomerDetails(metadata.getPrimaryCif());

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        final BeneficiaryDto beneficiary = validationContext.get("beneficiary-dto", BeneficiaryDto.class);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));

        final Set<MoneyTransferPurposeDto> allPurposeCodes = mobCommonService.getPaymentPurposes(request.getServiceType(), "INSTAREM", INDIVIDUAL_TYPE);
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));

        //Balance Validation
        final AccountDetailsDTO sourceAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("to-account-currency", beneficiary.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetails);

        FlexRuleEngineResponseDTO flexRuleEngineResponse = flexRuleEngineService.getRules(FlexRuleEngineMetadata.builder()
                        .channelTraceId(metadata.getChannelTraceId())
                        .cifId(metadata.getPrimaryCif())
                        .build(),
                FlexRuleEngineRequestDTO.builder()
                        .customerAccountNo(sourceAccountDetails.getNumber())
                        .accountCurrency(sourceAccountDetails.getCurrency())
                        .transactionCurrency(beneficiary.getBeneficiaryCurrency())
                        .transactionAmount(request.getAmount())
                        .beneficiaryId(beneficiary.getId())
                        .build()
        );

        validationContext.add("transfer-amount-in-source-currency", flexRuleEngineResponse.getAccountCurrencyAmount());
        responseHandler(balanceValidator.validate(request, metadata, validationContext));

        //Limit Validation
        final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetails,
                flexRuleEngineResponse.getAccountCurrencyAmount());

        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);

        final FundTransferContext fundTransferContext = new FundTransferContext();
        fundTransferContext.add(Constants.BENEFICIARY_FUND_CONTEXT_KEY, beneficiary);
        fundTransferContext.add(Constants.ACCOUNT_DETAILS_FUND_CONTEXT_KEY, sourceAccountDetails);
        fundTransferContext.add(Constants.CUSTOMER_DETAIL_FUND_CONTEXT_KEY, customerDetails);
        fundTransferContext.add(Constants.EXCHANGE_RATE_FUND_CONTEXT_KEY, flexRuleEngineResponse.getExchangeRate());
        fundTransferContext.add(Constants.TRANSFER_AMOUNT_IN_SRC_CURRENCY_FUND_CONTEXT_KEY, flexRuleEngineResponse.getAccountCurrencyAmount());
        fundTransferContext.add(Constants.FLEX_PRODUCT_CODE_CONTEXT_KEY, flexRuleEngineResponse.getProductCode());


        final QuickRemitFundTransferRequest quickRemitFundTransferRequest = mapper.mapTo(metadata, request, fundTransferContext);

        log.info("Quick Remit InstaRem Middleware started");
        final FundTransferResponse fundTransferResponse = quickRemitFundTransferMWService.transfer(quickRemitFundTransferRequest);

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();
    }

    private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
                                           final BigDecimal transferAmountInSrcCurrency) {
        return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
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
                .transactionCurrency("AED")
                .build();

        CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
        return currencyConversionDto.getTransactionAmount();
    }


}