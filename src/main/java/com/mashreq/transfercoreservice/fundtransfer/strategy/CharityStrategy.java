package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.SameAccountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_ACC_NOT_MATCH;

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

    @Override
    public void execute(FundTransferRequestDTO request, FundTransferMetadata metadata) {

        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", List.class, accountsFromCore);
        validateAccountContext.add("validate-from-account", Boolean.class, Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateAccountContext));


        CharityBeneficiaryDto charityBeneficiaryDto = beneficiaryClient.getCharity(request.getBeneficiaryId()).getData();

//        if (!toAccountNUmber.equals(charityBeneficiaryDto.getAccountNumber()))
//            GenericExceptionHandler.handleError(BENE_ACC_NOT_MATCH, BENE_ACC_NOT_MATCH.getErrorMessage());
//
//        if (!charityBeneficiaryDto.getCurrencyCode().equals(request.getCurrency()))
//            GenericExceptionHandler.handleError(ACCOUNT_CURRENCY_MISMATCH, ACCOUNT_CURRENCY_MISMATCH.getErrorMessage());



    }
}
