package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.time.Duration.between;
import static java.time.Instant.now;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CharityStrategyDefault implements FundTransferStrategy {

    private static final String INTERNAL_ACCOUNT_FLAG = "N";

    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final AccountService accountService;
    private final BeneficiaryClient beneficiaryClient;
    private final CharityValidator charityValidator;
    private final CurrencyValidator currencyValidator;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;
    private final BalanceValidator balanceValidator;



    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {

        Instant start = Instant.now();


        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));


        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validateContext = new ValidationContext();
        validateContext.add("account-details", accountsFromCore);
        validateContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateContext));


        Optional<AccountDetailsDTO> fromAccountOpt = accountsFromCore.stream()
                .filter(x -> request.getFromAccount().equals(x.getNumber()))
                .findFirst();

        //from account will always be present as it has been validated in the accountBelongsToCifValidator
        validateContext.add("from-account", fromAccountOpt.get());

        CharityBeneficiaryDto charityBeneficiaryDto = beneficiaryClient.getCharity(request.getBeneficiaryId()).getData();
        validateContext.add("charity-beneficiary-dto", charityBeneficiaryDto);
        validateContext.add("to-account-currency", charityBeneficiaryDto.getCurrencyCode());
        responseHandler(charityValidator.validate(request, metadata, validateContext));
        responseHandler(currencyValidator.validate(request, metadata, validateContext));

        //TODO

        //Balance Validation
        validateContext.add("transfer-amount-in-source-currency", request.getAmount());
        responseHandler(balanceValidator.validate(request, metadata, validateContext));



        //responseHandler(balanceValidator.validate(request, metadata,validateContext));

        // Assuming to account is always in AED

        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = request.getAmount();
        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit Validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountOpt.get(), charityBeneficiaryDto);
        final FundTransferResponse fundTransferResponse = fundTransferMWService.transfer(fundTransferRequest, metadata);


        //final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);


        log.info("Total time taken for {} strategy {} milli seconds ", request.getServiceType(), between(start, now()).toMillis());

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private FundTransferRequest prepareFundTransferRequestPayload(RequestMetaData metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO sourceAccount, CharityBeneficiaryDto charityBeneficiaryDto) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(charityBeneficiaryDto.getAccountNumber())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(sourceAccount.getCurrency())
                .sourceBranchCode(sourceAccount.getBranchCode())
                .beneficiaryFullName(charityBeneficiaryDto.getName())
                .destinationCurrency(charityBeneficiaryDto.getCurrencyCode())
                .transactionCode("096")
                .internalAccFlag(INTERNAL_ACCOUNT_FLAG)
                .build();

    }
}
