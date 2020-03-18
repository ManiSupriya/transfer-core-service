package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Long.valueOf;

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
    private final BeneficiaryClient beneficiaryClient;

    @Override
    public void execute(FundTransferRequestDTO request,FundTransferMetadata metadata) {

        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());
        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", List.class, accountsFromCore);
        validationContext.add("validate-from-account", List.class, accountsFromCore);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));


        BeneficiaryDto beneficiaryDto = beneficiaryClient.getBydId(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()))
                .getData();

        validationContext.add("beneficiary-dto", BeneficiaryDto.class, beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        responseHandler(currencyValidator.validate(request, metadata, validationContext));
    }
}
