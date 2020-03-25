package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.fundtransfer.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
import static java.lang.Long.valueOf;

/**
 *
 */
@AllArgsConstructor
@Slf4j
@Service
public class LocalFundTransferStrategy implements FundTransferStrategy {

    private final IBANValidator ibanValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryClient beneficiaryClient;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;

    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {
        responseHandler(finTxnNoValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));
        log.info("Account belongs to cif validation successful");

        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());

        BeneficiaryDto beneficiaryDto = beneficiaryClient.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()))
                .getData();
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        log.info("Beneficiary validation successful");

        responseHandler(ibanValidator.validate(request, metadata, validationContext));
        log.info("IBAN validation successful");

        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), request.getAmount());
        log.info("Limit validation successful");

        log.info("Local Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.sendMoneyToIBAN(metadata, request, fromAccountDetails);
        log.info("Local Fund transfer successful........");

        return fundTransferResponse.toBuilder().limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }

}
