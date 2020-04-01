package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static java.time.Duration.between;
import static java.time.Instant.now;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CharityStrategy implements FundTransferStrategy {

    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final AccountService accountService;
    private final BeneficiaryClient beneficiaryClient;
    private final CharityValidator charityValidator;
    private final CurrencyValidator currencyValidator;
    private final LimitValidator limitValidator;
    private final CoreTransferService coreTransferService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {

        Instant start = Instant.now();

        Instant finValidatorStart = Instant.now();
        responseHandler(finTxnNoValidator.validate(request, metadata));
        log.info("Total time taken for {} finTxnNoValidator milli seconds ", request.getServiceType(), between(finValidatorStart, now()).toMillis());

        responseHandler(sameAccountValidator.validate(request, metadata));

        Instant accountStart = Instant.now();
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validateContext = new ValidationContext();
        validateContext.add("account-details", accountsFromCore);
        validateContext.add("validate-from-account", Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateContext));
        log.info("Total time taken for {} accountBelongsToCifValidator milli seconds ", request.getServiceType(), between(accountStart, now()).toMillis());

        Instant charityStart = Instant.now();
        CharityBeneficiaryDto charityBeneficiaryDto = beneficiaryClient.getCharity(request.getBeneficiaryId()).getData();
        validateContext.add("charity-beneficiary-dto", charityBeneficiaryDto);
        responseHandler(charityValidator.validate(request, metadata, validateContext));
        log.info("Total time taken for {} charityValidator milli seconds ", request.getServiceType(), between(charityStart, now()).toMillis());

        responseHandler(currencyValidator.validate(request, metadata, validateContext));

        // Assuming to account is always in AED
        Instant limitStart = Instant.now();
        log.info("Limit Validation start.");
        BigDecimal limitUsageAmount = request.getAmount();
        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit Validation successful");
        log.info("Total time taken for {} limitValidator milli seconds ", request.getServiceType(), between(limitStart, now()).toMillis());

        Instant coreTransStart = Instant.now();
        final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);
        log.info("Total time taken for {} limitValidator milli seconds ", request.getServiceType(), between(coreTransStart, now()).toMillis());

        log.info("Total time taken for {} strategy {} milli seconds ", request.getServiceType(), between(start, now()).toMillis());

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }
}
