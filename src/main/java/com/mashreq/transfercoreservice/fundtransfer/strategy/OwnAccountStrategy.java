package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@AllArgsConstructor
@Service
public class OwnAccountStrategy implements FundTransferStrategy {

    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final SameAccountValidator sameAccountValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final CurrencyValidator currencyValidator;
    private AccountService accountService;

    @Override
    public void execute(FundTransferRequestDTO request,FundTransferMetadata metadata) {

        responseHandler(finTxnNoValidator.validate(request, metadata));
        responseHandler(sameAccountValidator.validate(request, metadata));

        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", List.class, accountsFromCore);
        validateAccountContext.add("validate-to-account", Boolean.class, Boolean.TRUE);
        validateAccountContext.add("validate-from-account", Boolean.class, Boolean.TRUE);
        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validateAccountContext));





        // validate fin txn no
        // validate account numbers
        // find digital user
        // find user dto
        // validate beneficiary
        // validate limit

    }
}
