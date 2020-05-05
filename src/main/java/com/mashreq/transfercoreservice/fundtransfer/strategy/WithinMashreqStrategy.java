package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.lang.Long.valueOf;
import static java.time.Duration.between;
import static java.time.Instant.now;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class WithinMashreqStrategy implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";

    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final CurrencyValidator currencyValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final LimitValidator limitValidator;
    private final CoreTransferService coreTransferService;
    private final MaintenanceService maintenanceService;
    private final BalanceValidator balanceValidator;
    private final FundTransferMWService fundTransferMWService;

    @Value("${app.uae.transaction.code:096}")
    private String transactionCode;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {

        Instant start = Instant.now();

        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));

        Optional<AccountDetailsDTO> fromAccountOpt = accountsFromCore.stream()
                .filter(x -> request.getFromAccount().equals(x.getNumber()))
                .findFirst();

        //from account will always be present as it has been validated in the accountBelongsToCifValidator
        validationContext.add("from-account", fromAccountOpt.get());

        BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()));
        validationContext.add("to-account-currency",beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        responseHandler(currencyValidator.validate(request, metadata, validationContext));

        //TODO
        //responseHandler(balanceValidator.validate(request,metadata,validationContext));

        // As per current implementation with FE they are sending toCurrency and its value for within and own
        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = request.getAmount();
        if (!userDTO.getLocalCurrency().equalsIgnoreCase(request.getCurrency())) {

            // Since we support request currency it can be  debitLeg or creditLeg
            String givenAccount = request.getToAccount();
            if (request.getCurrency().equalsIgnoreCase(fromAccountOpt.get().getCurrency())) {
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



        log.info("Total time taken for {} strategy {} milli seconds ", request.getServiceType(), between(start, now()).toMillis());


        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountOpt.get(), beneficiaryDto);
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest);


        //final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, BeneficiaryDto beneficiaryDto) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(beneficiaryDto.getAccountNumber())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(sourceAccount.getCurrency())
                .sourceBranchCode(sourceAccount.getBranchCode())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .destinationCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .transactionCode(transactionCode)
                .internalAccFlag(INTERNAL_ACCOUNT_FLAG)
                .build();

    }
}
