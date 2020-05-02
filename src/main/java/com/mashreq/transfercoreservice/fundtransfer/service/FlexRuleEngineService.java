package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMWRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    private final BeneficiaryService beneficiaryService;
    private final AccountService accountService;

    private final FlexRuleEngineMWService flexRuleEngineMWService;

    public FlexRuleEngineResponseDTO getRules(final FlexRuleEngineMetadata metadata, final FlexRuleEngineRequestDTO request) {

        assertEitherDebitOrCreditAmountPresent(request);

        final String valueDate = DateTimeFormatter.ofPattern(DATE_FORMAT).format(LocalDateTime.now());

        final CompletableFuture<SearchAccountDto> searchAccountFut = CompletableFuture.supplyAsync(() ->
                accountService.getAccountDetailsFromCore(request.getCustomerAccountNo()));

        final BeneficiaryDto beneficiary = beneficiaryService.getById(metadata.getCifId(), Long.valueOf(request.getBeneficiaryId()));

        final SearchAccountDto searchAccountDto = searchAccountFut.join();

        assertReqCurrencyMatchesBeneCurrency(request, searchAccountDto);


        if (nonNull(request.getTransactionAmount())) {
            log.info("Calling Flex Rule MW with Debit Leg {} {} ", request.getTransactionCurrency(), request.getTransactionAmount());
            return flexRuleEngineMWService.getRules(getFlexRequestWithCreditLeg(metadata, request, valueDate, beneficiary));

        } else {
            log.info("Calling Flex Rule MW with Credit Leg {} {} ", request.getAccountCurrencyAmount(), request.getAccountCurrency());
            return flexRuleEngineMWService.getRules(getFlexRequestWithDebitLeg(metadata, request, valueDate, beneficiary));
        }
    }

    private void assertReqCurrencyMatchesBeneCurrency(FlexRuleEngineRequestDTO request, SearchAccountDto searchAccountDto) {
        if (request.getAccountCurrency().equals(searchAccountDto.getCurrency())) {
            GenericExceptionHandler.handleError(BENE_CUR_NOT_MATCH, BENE_CUR_NOT_MATCH.getErrorMessage());
        }
    }


    private void assertEitherDebitOrCreditAmountPresent(FlexRuleEngineRequestDTO request) {
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
