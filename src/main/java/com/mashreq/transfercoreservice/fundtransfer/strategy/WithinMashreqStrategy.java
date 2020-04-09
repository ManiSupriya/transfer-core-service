package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        //responseHandler(balanceValidator.validate(request,metadata,validationContext));

        log.info("Total time taken for {} strategy {} milli seconds ", request.getServiceType(), between(start, now()).toMillis());


        final FundTransferResponse fundTransferResponse = coreTransferService.transferFundsBetweenAccounts(request);

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }
}
