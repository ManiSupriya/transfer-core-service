package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.sun.org.apache.xpath.internal.operations.Bool;
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

    private AccountBelongsToCifValidator accountBelongsToCifValidator;
    private AccountService accountService;

    @Override
    public void execute(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        ValidationContext validateAccountContext = new ValidationContext();
        validateAccountContext.add("account-details", List.class, accountsFromCore);
        validateAccountContext.add("validate-to-account", Boolean.class, Boolean.TRUE);
        validateAccountContext.add("validate-from-account", Boolean.class, Boolean.TRUE);

        accountBelongsToCifValidator.validate(request, metadata, validateAccountContext);
        // validate fin txn no
        // validate account numbers
        // find digital user
        // find user dto
        // validate beneficiary
        // validate limit

    }
}
