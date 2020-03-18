package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.fundtransfer.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.OwnAccountStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.WithinMashreqStrategy;
import com.mashreq.transfercoreservice.fundtransfer.validators.FinTxnNoValidator;
import com.mashreq.transfercoreservice.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus.ACTIVE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static com.mashreq.transfercoreservice.fundtransfer.ServiceType.OWN_ACCOUNT;
import static com.mashreq.transfercoreservice.fundtransfer.ServiceType.WITHIN_MASHREQ;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    private final CoreTransferService coreTransferService;
    private final DigitalUserRepository digitalUserRepository;
    private final LimitValidator limitValidator;
    private final PaymentHistoryService paymentHistoryService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final BeneficiaryClient beneficiaryClient;
    private final AccountService accountService;
    private final OwnAccountStrategy ownAccountStrategy;
    private final WithinMashreqStrategy withinMashreqStrategy;
    private EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;

    @PostConstruct
    public void init() {
        fundTransferStrategies = new EnumMap<>(ServiceType.class);
        fundTransferStrategies.put(OWN_ACCOUNT, ownAccountStrategy);
        fundTransferStrategies.put(WITHIN_MASHREQ, withinMashreqStrategy);
    }

    @Override
    public PaymentHistoryDTO transferFund(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        log.info("Starting fund transfer for {} ", request.getServiceType());

        FundTransferStrategy strategy = fundTransferStrategies.get(request.getServiceType());
        strategy.execute(request,metadata);

        log.info("Finding Digital User for CIF-ID {}", metadata.getPrimaryCif());
        DigitalUser digitalUser = getDigitalUser(metadata);

        log.info("Creating  User DTO");
        UserDTO userDTO = createUserDTO(metadata, digitalUser);

        LimitValidatorResultsDto validationResult = limitValidator.validate(userDTO, request.getServiceType(), request.getAmount());
        log.info("Limit Validation successful");

        CoreFundTransferRequestDto coreFundTransferRequestDto = CoreFundTransferRequestDto.builder()
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .dealNumber(request.getDealNumber())
                .purposeCode(request.getPurposeCode())
                .build();

        log.info("Calling external service for fundtransfer {} ", coreFundTransferRequestDto);
        CoreFundTransferResponseDto response = coreTransferService.transferFundsBetweenAccounts(coreFundTransferRequestDto);

        if (response.getMwResponseStatus().equals(MwResponseStatus.S)) {
            String versionUuid = validationResult.getLimitVersionUuid();
            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(request, userDTO, metadata, versionUuid);
            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
            digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
        }

        // Insert payment history irrespective of mw payment fails or success
        PaymentHistoryDTO paymentHistoryDTO = generatePaymentHistory(request, response, userDTO, metadata);

        log.info("Inserting into Payments History table {} ", paymentHistoryDTO);
        paymentHistoryService.insert(paymentHistoryDTO);

        if (MwResponseStatus.F.equals(response.getMwResponseStatus())) {
            GenericExceptionHandler.handleError(response.getTransferErrorCode(),
                    String.format("%s : %s ", request.getFinTxnNo(), response.getExternalErrorMessage()));
        }

        return paymentHistoryDTO;
    }

    private void validateAccountNumbers(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        String toAccountNUmber = request.getToAccount();
        String fromAccountNumber = request.getFromAccount();

        if (toAccountNUmber.equals(fromAccountNumber))
            GenericExceptionHandler.handleError(CREDIT_AND_DEBIT_ACC_SAME, CREDIT_AND_DEBIT_ACC_SAME.getErrorMessage());


        List<AccountDetailsDTO> coreAccounts = accountService.getAccountsFromCore(metadata.getPrimaryCif());

        log.info("Validating account belong to same cif for own-account transfer");

        if (OWN_ACCOUNT.getName().equals(request.getServiceType())) {

            if (!isAccountNumberBelongsToCif(coreAccounts, toAccountNUmber))
                GenericExceptionHandler.handleError(ACCOUNT_NOT_BELONG_TO_CIF, ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage());

            if (!isAccountNumberBelongsToCif(coreAccounts, fromAccountNumber))
                GenericExceptionHandler.handleError(ACCOUNT_NOT_BELONG_TO_CIF, ACCOUNT_NOT_BELONG_TO_CIF.getErrorMessage());

            if (!validateToAccountCurrency(coreAccounts, request.getCurrency(), request.getToAccount()))
                GenericExceptionHandler.handleError(ACCOUNT_CURRENCY_MISMATCH, ACCOUNT_CURRENCY_MISMATCH.getErrorMessage());


        } else if (ServiceType.CHARITY_ACCOUNT.getName().equals(request.getServiceType())) {

            CharityBeneficiaryDto charityBeneficiaryDto = beneficiaryClient.getCharity(request.getBeneficiaryId()).getData();

            if (!toAccountNUmber.equals(charityBeneficiaryDto.getAccountNumber()))
                GenericExceptionHandler.handleError(BENE_ACC_NOT_MATCH, BENE_ACC_NOT_MATCH.getErrorMessage());

            if (!charityBeneficiaryDto.getCurrencyCode().equals(request.getCurrency()))
                GenericExceptionHandler.handleError(ACCOUNT_CURRENCY_MISMATCH, ACCOUNT_CURRENCY_MISMATCH.getErrorMessage());

        } else {
            //TODO Discuss with Bala
            //TODO All other transfer modes should have to-account which should not belong to sender's cif
            if (isAccountNumberBelongsToCif(coreAccounts, toAccountNUmber))
                GenericExceptionHandler.handleError(TO_ACCOUNT_BELONGS_TO_SAME_CIF, TO_ACCOUNT_BELONGS_TO_SAME_CIF.getErrorMessage());
        }
    }


    private boolean validateToAccountCurrency(List<AccountDetailsDTO> coreAccounts, String toAcctCurrency, String toAccountNum) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(toAccountNum))
                .anyMatch(account -> account.getCurrency().equals(toAcctCurrency));
    }

    private boolean isAccountNumberBelongsToCif(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .anyMatch(x -> x.getNumber().equals(accountNumber));
    }

    private void validateFinTxnNo(FundTransferRequestDTO request) {
        if (paymentHistoryService.isFinancialTransactionPresent(request.getFinTxnNo())) {
            GenericExceptionHandler.handleError(DUPLICATION_FUND_TRANSFER_REQUEST, DUPLICATION_FUND_TRANSFER_REQUEST.getErrorMessage());
        }
    }

    private void validateBeneficiary(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        if (StringUtils.isNotBlank(request.getBeneficiaryId())
                && !request.getServiceType().equalsIgnoreCase("own-account")) {

            BeneficiaryDto beneficiaryDto = beneficiaryClient.getBydId(
                    metadata.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId())).getData();

            if (beneficiaryDto == null)
                GenericExceptionHandler.handleError(BENE_NOT_FOUND, BENE_NOT_FOUND.getErrorMessage());

            if (!beneficiaryDto.getAccountNumber().equals(request.getToAccount()))
                GenericExceptionHandler.handleError(BENE_ACC_NOT_MATCH, BENE_ACC_NOT_MATCH.getErrorMessage());

            if (!request.getCurrency().equals(beneficiaryDto.getCurrency()))
                GenericExceptionHandler.handleError(BENE_CUR_NOT_MATCH, BENE_CUR_NOT_MATCH.getErrorMessage());

            if (ACTIVE != beneficiaryDto.getStatus())
                GenericExceptionHandler.handleError(BENE_NOT_ACTIVE, BENE_NOT_ACTIVE.getErrorMessage());

        }


    }


    private DigitalUser getDigitalUser(FundTransferMetadata fundTransferMetadata) {
        Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
        if (!digitalUserOptional.isPresent()) {
            GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
        }
        log.info("Digital User found successfully {} ", digitalUserOptional.get());
        return digitalUserOptional.get();
    }

    private UserDTO createUserDTO(FundTransferMetadata fundTransferMetadata, DigitalUser digitalUser) {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId(fundTransferMetadata.getPrimaryCif());
        userDTO.setUserId(digitalUser.getId());
        userDTO.setSegmentId(digitalUser.getDigitalUserGroup().getSegment().getId());
        userDTO.setCountryId(digitalUser.getDigitalUserGroup().getCountry().getId());

        log.info("User DTO  created {} ", userDTO);
        return userDTO;
    }

    private DigitalUserLimitUsageDTO generateUserLimitUsage(FundTransferRequestDTO request, UserDTO userDTO,
                                                            FundTransferMetadata fundTransferMetadata, String versionUuid) {
        return DigitalUserLimitUsageDTO.builder()
                .digitalUserId(userDTO.getUserId())
                .cif(fundTransferMetadata.getPrimaryCif())
                .channel(fundTransferMetadata.getChannel())
                .beneficiaryTypeCode(request.getServiceType())
                .paidAmount(request.getAmount())
                .versionUuid(versionUuid)
                .createdBy(String.valueOf(userDTO.getUserId()))
                .build();

    }

    PaymentHistoryDTO generatePaymentHistory(FundTransferRequestDTO request, CoreFundTransferResponseDto coreResponse, UserDTO userDTO,
                                             FundTransferMetadata fundTransferMetadata) {

        //convert dto
        return PaymentHistoryDTO.builder()
                .cif(fundTransferMetadata.getPrimaryCif())
                .userId(userDTO.getUserId())
                .accountTo(request.getToAccount())
                .beneficiaryTypeCode(request.getServiceType())
                .channel(fundTransferMetadata.getChannel())
                //.billRefNo(coreResponse.getBillRefNo())
                .ipAddress(fundTransferMetadata.getChannelHost())
                .paidAmount(request.getAmount())
                //.dueAmount(request.getDueAmount())
                //.toCurrency(PaymentConstants.BILL_PAYMENT_TO_CURRENCY)
                .status(coreResponse.getMwResponseStatus().name())
                .mwReferenceNo(coreResponse.getMwReferenceNo())
                .mwResponseCode(coreResponse.getMwResponseCode())
                .mwResponseDescription(coreResponse.getMwResponseDescription())
                .accountFrom(request.getFromAccount())
                .financialTransactionNo(request.getFinTxnNo())
                //.encryptedCardFrom(request.getDebitAccountNo())
//                .encryptedCardFromFourdigit(
//                        StringUtils.isEmpty(request.getDebitAccountNo()) ? null :
//                                (request.getDebitAccountNo().substring(request.getDebitAccountNo().length() - 4)))
                .build();

    }
}
