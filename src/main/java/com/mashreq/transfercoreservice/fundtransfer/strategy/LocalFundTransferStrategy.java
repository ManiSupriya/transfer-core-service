package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.fundtransfer.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static java.lang.Long.valueOf;

/**
 *
 */
@AllArgsConstructor
@Slf4j
@Service
public class LocalFundTransferStrategy implements FundTransferStrategy {

    private static final String LOCAL = "LOCAL";
    private final IBANValidator ibanValidator;
    private final FinTxnNoValidator finTxnNoValidator;
    private final AccountBelongsToCifValidator accountBelongsToCifValidator;
    private final BeneficiaryValidator beneficiaryValidator;
    private final AccountService accountService;
    private final BeneficiaryClient beneficiaryClient;
    private final LimitValidator limitValidator;
    private final FundTransferMWService fundTransferMWService;
    private final MaintenanceClient maintenanceClient;
    private final PaymentPurposeValidator paymentPurposeValidator;
    private final BalanceValidator balanceValidator;

   /* @Value("${app.local.currency}")
    private String localCurrency;*/


    @Override
    public FundTransferResponse execute(FundTransferRequestDTO request, FundTransferMetadata metadata, UserDTO userDTO) {
        request.setCurrency("AED");
        responseHandler(finTxnNoValidator.validate(request, metadata));
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        final ValidationContext validationContext = new ValidationContext();
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("validate-from-account", Boolean.TRUE);

        final Set<PurposeOfTransferDto> allPurposeCodes = maintenanceClient.getAllPurposeCodes(LOCAL).getData();
        validationContext.add("purposes", allPurposeCodes);
        responseHandler(paymentPurposeValidator.validate(request, metadata, validationContext));
        log.info("Purpose code and description validation successful");

        responseHandler(accountBelongsToCifValidator.validate(request, metadata, validationContext));
        log.info("Account belongs to cif validation successful");
        final AccountDetailsDTO fromAccountDetails = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
        validationContext.add("from-account", fromAccountDetails);

        final BeneficiaryDto beneficiaryDto = beneficiaryClient.getById(metadata.getPrimaryCif(), valueOf(request.getBeneficiaryId()))
                .getData();
        validationContext.add("beneficiary-dto", beneficiaryDto);
        responseHandler(beneficiaryValidator.validate(request, metadata, validationContext));
        log.info("Beneficiary validation successful");

        responseHandler(ibanValidator.validate(request, metadata, validationContext));
        log.info("IBAN validation successful");

        responseHandler(balanceValidator.validate(request, metadata, validationContext));
        log.info("Balance validation successful");

        log.info("Limit Validation start.");
        // Assuming to account is always in local currency so on currency conversion required
        BigDecimal limitUsageAmount = request.getAmount();
        final LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), limitUsageAmount);
        log.info("Limit validation successful");

        final FundTransferRequest fundTransferRequest = prepareFundTransferRequestPayload(metadata, request, fromAccountDetails, beneficiaryDto);
        log.info("Local Fund transfer initiated.......");
        final FundTransferResponse fundTransferResponse = fundTransferMWService.sendMoneyToIBAN(fundTransferRequest);
        log.info("Local Fund transfer successful........");

        return fundTransferResponse.toBuilder()
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(validationResult.getLimitVersionUuid()).build();

    }

    private FundTransferRequest prepareFundTransferRequestPayload(FundTransferMetadata metadata, FundTransferRequestDTO request,
                                                                  AccountDetailsDTO accountDetails, BeneficiaryDto beneficiaryDto) {
        return FundTransferRequest.builder()
                .amount(request.getAmount())
                .channel(metadata.getChannel())
                .channelTraceId(metadata.getChannelTraceId())
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .purposeCode(request.getPurposeCode())
                .purposeDesc(request.getPurposeDesc())
                .chargeBearer(request.getChargeBearer())
                .finTxnNo(request.getFinTxnNo())
                .sourceCurrency(accountDetails.getCurrency())
                .sourceBranchCode(accountDetails.getBranchCode())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .destinationBankName(beneficiaryDto.getBankName())
                .swiftCode(beneficiaryDto.getSwiftCode())
                .build();

    }


    private AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }



}
