package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * @author shahbazkh
 * @date 4/29/20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FlexRuleEngineService {

    public static final String INDIA_COUNTRY_CODE = "IN";
    public static final String INDIA_DEFAULT_SWIFT_CODE = "XXXXIN";
    public static final String DEFAULT_TRANSFER_TYPE = "AC";
    public static final String DEFAULT_TRANSACTION_STATUS = "STP";
    public static final String DATE_FORMAT = "YYYY-MM-dd";
    public static final String INDIA_CODE = "IN";
    public static final String PAKISTAN_CODE = "PK";
    private final BeneficiaryService beneficiaryService;
    private final AccountService accountService;

    private final FlexRuleEngineMWService flexRuleEngineMWService;
    private final MaintenanceService maintenanceService;

    /**
     * Fetch Charges
     *
     * @param metadata
     * @param request
     * @return
     */
    public ChargeResponseDTO getCharges(final FlexRuleEngineMetadata metadata, final FlexRuleEngineRequestDTO request, final RequestMetaData metaData) {

        final String valueDate = ofPattern(DATE_FORMAT).format(now());

        final CompletableFuture<SearchAccountDto> searchAccountFut = CompletableFuture.supplyAsync(() ->
                accountService.getAccountDetailsFromCore(request.getCustomerAccountNo()));

        final BeneficiaryDto beneficiary = beneficiaryService.getById(metadata.getCifId(), Long.valueOf(request.getBeneficiaryId()), metaData);

        final SearchAccountDto searchAccountDto = searchAccountFut.join();

        assertAccountCurrencyMatch(request, searchAccountDto);

        log.info("Calling Flex Rule MW for CHARGES with Debit Leg {} {} ", htmlEscape(request.getAccountCurrency()), htmlEscape(request.getAccountCurrencyAmount().toString()));

        final FlexRuleEngineMWResponse response = flexRuleEngineMWService.getRules(
                getFlexRequestWithDebitLeg(metadata, request, valueDate, beneficiary), metaData);

        log.info("Debit Amount  = {} {} ", htmlEscape(request.getAccountCurrency()), htmlEscape(request.getAccountCurrencyAmount().toString()));

        if (!request.getAccountCurrency().equals(response.getChargeCurrency())) {
            return getChargeWithConvertedCurrency(request, response);
        }

        return getCharge(request, response);

    }

    private ChargeResponseDTO getCharge(FlexRuleEngineRequestDTO request, FlexRuleEngineMWResponse response) {
        final BigDecimal chargeAmount = new BigDecimal(response.getChargeAmount());
        log.info("Charge in Debit Currency = {} {} ", htmlEscape(request.getAccountCurrency()), htmlEscape(chargeAmount.toString()));

        final BigDecimal totalDebitAmount = request.getAccountCurrencyAmount().add(chargeAmount);
        log.info("Total Debit Amount = {} {} ", htmlEscape(request.getAccountCurrency()), htmlEscape(totalDebitAmount.toString()));

        return ChargeResponseDTO.builder()
                .flexChargeAmount(chargeAmount)
                .flexChargeCurrency(response.getChargeCurrency())
                .chargeCurrency(response.getChargeCurrency())
                .chargeAmount(chargeAmount)
                .totalDebitAmount(totalDebitAmount)
                .build();
    }

    private ChargeResponseDTO getChargeWithConvertedCurrency(FlexRuleEngineRequestDTO request, FlexRuleEngineMWResponse response) {
        CurrencyConversionDto convertedCurrency = getConvertedChargeAmount(request, response);
        log.info("Charge in Debit Currency = {} {} ", htmlEscape(request.getAccountCurrency()), htmlEscape(convertedCurrency.getAccountCurrencyAmount().toString()));
        final BigDecimal totalDebitAmount = request.getAccountCurrencyAmount().add(convertedCurrency.getAccountCurrencyAmount());
        log.info("Total Debit Amount = {} {} ", htmlEscape(request.getAccountCurrency()), htmlEscape(totalDebitAmount.toString()));

        return ChargeResponseDTO.builder()
                .flexChargeCurrency(response.getChargeCurrency())
                .flexChargeAmount(new BigDecimal(response.getChargeAmount()))
                .chargeCurrency(request.getAccountCurrency())
                .chargeAmount(convertedCurrency.getAccountCurrencyAmount())
                .debitAmount(request.getAccountCurrencyAmount())
                .totalDebitAmount(request.getAccountCurrencyAmount().add(convertedCurrency.getAccountCurrencyAmount()))
                .build();
    }

    private CurrencyConversionDto getConvertedChargeAmount(FlexRuleEngineRequestDTO request, FlexRuleEngineMWResponse response) {
        log.info("Debit Account Currency = {} and Charge Currency = {} calling currency conversion with Product code = {} ",
                htmlEscape(request.getAccountCurrency()), htmlEscape(response.getChargeCurrency()), htmlEscape(response.getProductCode()));
        CoreCurrencyConversionRequestDto coreCurrencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        coreCurrencyConversionRequestDto.setAccountNumber(request.getCustomerAccountNo());
        coreCurrencyConversionRequestDto.setAccountCurrency(request.getAccountCurrency());
        coreCurrencyConversionRequestDto.setTransactionCurrency(response.getChargeCurrency());
        coreCurrencyConversionRequestDto.setTransactionAmount(new BigDecimal(response.getChargeAmount()));
        coreCurrencyConversionRequestDto.setProductCode(response.getProductCode());
        return maintenanceService.convertBetweenCurrencies(coreCurrencyConversionRequestDto);
    }

    /**
     * Fetch Rules Remitance
     *
     * @param metadata
     * @param request
     * @return
     */
    public FlexRuleEngineResponseDTO getRules(final FlexRuleEngineMetadata metadata, final FlexRuleEngineRequestDTO request, final RequestMetaData metaData) {

        assertEitherDebitOrCreditAmountPresent(request);
        final CompletableFuture<SearchAccountDto> searchAccountFut = CompletableFuture.supplyAsync(() ->
                accountService.getAccountDetailsFromCore(request.getCustomerAccountNo()));

        final CompletableFuture<BeneficiaryDto> beneficiaryDtoFut = CompletableFuture.supplyAsync(() ->
                beneficiaryService.getById(metadata.getCifId(), Long.valueOf(request.getBeneficiaryId()), metaData));

        final SearchAccountDto searchAccountDto = searchAccountFut.join();

        assertAccountCurrencyMatch(request, searchAccountDto);

        final BeneficiaryDto beneficiary = beneficiaryDtoFut.join();

        assertBeneficiaryCurrencyMatch(request, beneficiary);

        final FlexRuleEngineMWRequest flexRequestMW = nonNull(request.getTransactionAmount())
                ? getFlexRequestWithCreditLeg(metadata, request, ofPattern(DATE_FORMAT).format(now()), beneficiary)
                : getFlexRequestWithDebitLeg(metadata, request, ofPattern(DATE_FORMAT).format(now()), beneficiary);

        final FlexRuleEngineMWResponse response = flexRuleEngineMWService.getRules(flexRequestMW, metaData);


        if (INDIA_CODE.equals(beneficiary.getBeneficiaryCountryISO())
                || PAKISTAN_CODE.equalsIgnoreCase(beneficiary.getBeneficiaryCountryISO())) {

            final FlexRuleEngineResponseDTO flexRuleEngineResponse = FlexRuleEngineResponseDTO.builder()
                    .productCode(response.getProductCode())
                    .build();
            log.info("Flex Rule Engine Response  {} ", flexRuleEngineResponse);

            return flexRuleEngineResponse;
        } else {

            final FlexRuleEngineResponseDTO flexRuleEngineResponse = FlexRuleEngineResponseDTO.builder()
                    .productCode(response.getProductCode())
                    .exchangeRate(response.getExchangeRate())
                    .accountCurrencyAmount(response.getAccountCurrencyAmount())
                    .transactionAmount(response.getTransactionAmount())
                    .build();
            log.info("Flex Rule Engine Response (INSTAREM)  {} ", flexRuleEngineResponse);

            return flexRuleEngineResponse;
        }


    }

    private void assertAccountCurrencyMatch(FlexRuleEngineRequestDTO request, SearchAccountDto searchAccountDto) {
        if (!request.getAccountCurrency().equals(searchAccountDto.getCurrency())) {
            GenericExceptionHandler.handleError(ACCOUNT_CURRENCY_MISMATCH, ACCOUNT_CURRENCY_MISMATCH.getErrorMessage());
        }
    }

    private void assertBeneficiaryCurrencyMatch(FlexRuleEngineRequestDTO request, BeneficiaryDto beneficiaryDto) {
        if (!request.getTransactionCurrency().equals(beneficiaryDto.getBeneficiaryCurrency())) {
            GenericExceptionHandler.handleError(BENE_CUR_NOT_MATCH, BENE_CUR_NOT_MATCH.getErrorMessage());
        }
    }


    private void assertEitherDebitOrCreditAmountPresent(FlexRuleEngineRequestDTO request) {
        if (Objects.nonNull(request.getTransactionAmount()) && Objects.nonNull(request.getAccountCurrencyAmount())) {
            GenericExceptionHandler.handleError(FLEX_RULE_ONLY_1_AMOUNT_ALLLOWED, FLEX_RULE_ONLY_1_AMOUNT_ALLLOWED.getErrorMessage());
        }

        if (isNull(request.getTransactionAmount()) && isNull(request.getAccountCurrencyAmount())) {
            GenericExceptionHandler.handleError(FLEX_RULE_EITHER_DEBIT_OR_CREDIT_AMT_REQUIRED, FLEX_RULE_EITHER_DEBIT_OR_CREDIT_AMT_REQUIRED.getErrorMessage());
        }
    }

    private FlexRuleEngineMWRequest getFlexRequestWithDebitLeg(FlexRuleEngineMetadata metadata, FlexRuleEngineRequestDTO request, String valueDate, BeneficiaryDto beneficiary) {
        return FlexRuleEngineMWRequest.builder()
                .transactionCurrency(request.getTransactionCurrency())
                .accountCurrency(request.getAccountCurrency())
                .accountCurrencyAmount(String.valueOf(request.getAccountCurrencyAmount()))
                .customerAccountNo(request.getCustomerAccountNo())
                .accountWithInstitution(getSwiftCode(beneficiary))
                .transactionStatus(DEFAULT_TRANSACTION_STATUS)
                .transferType(DEFAULT_TRANSFER_TYPE)
                .valueDate(valueDate)
                .channelTraceId(metadata.getChannelTraceId())
                .build();
    }

    private FlexRuleEngineMWRequest getFlexRequestWithCreditLeg(FlexRuleEngineMetadata metadata, FlexRuleEngineRequestDTO request, String valueDate, BeneficiaryDto beneficiary) {
        return FlexRuleEngineMWRequest.builder()
                .transactionCurrency(request.getTransactionCurrency())
                .transactionAmount(valueOf(request.getTransactionAmount()))
                .accountCurrency(request.getAccountCurrency())
                .customerAccountNo(request.getCustomerAccountNo())
                .accountWithInstitution(getSwiftCode(beneficiary))
                .transactionStatus(DEFAULT_TRANSACTION_STATUS)
                .transferType(DEFAULT_TRANSFER_TYPE)
                .valueDate(valueDate)
                .channelTraceId(metadata.getChannelTraceId())
                .build();
    }

    /*
     * Default value is used only in case of India
     */
    private String getSwiftCode(BeneficiaryDto beneficiary) {
        return INDIA_COUNTRY_CODE.equals(beneficiary.getBeneficiaryCountryISO())
                ? INDIA_DEFAULT_SWIFT_CODE
                : beneficiary.getSwiftCode();
    }
}
