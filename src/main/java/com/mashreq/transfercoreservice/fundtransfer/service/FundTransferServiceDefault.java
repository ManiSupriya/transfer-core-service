package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.service.CoreTransferService;
import com.mashreq.transfercoreservice.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.limits.LimitValidator;
import com.mashreq.transfercoreservice.limits.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    //    private final OwnAccountStrategy ownAccountStrategy;
//    private final WithinMashreqStrategy withinMashreqStrategy;
    private final CoreTransferService coreTransferService;
    private final DigitalUserRepository digitalUserRepository;
    private final LimitValidator limitValidator;
    private final PaymentHistoryService paymentHistoryService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;

    private EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;

    @PostConstruct
    public void init() {
        fundTransferStrategies = new EnumMap<>(ServiceType.class);
//        fundTransferStrategies.put(OWN_ACCOUNT, ownAccountStrategy);
//        fundTransferStrategies.put(WITHIN_MASHREQ, withinMashreqStrategy);
    }

    /**
     * 1. validate financial_trx_no for the given request
     * <p>
     * 1. user CIF exists or not
     * <p>
     * 2. if within cif then both account should be of same user
     * 3. if another mashreq then to account should be of user with given CIF
     * <p>
     * 1. source account should be active  - already in transfer call of account-service
     * 2. destination should be active/dormant - already in transfer call of account-service
     * 3. validate transactionAmount with respect to available balance of source account - already in transfer call of account-service
     * <p>
     * <p>
     * 3.1 call mw which does all 1,2,3
     * <p>
     * 3. validate limit ( copy from bill payment )
     * 4. update limit on success
     * 5. store payment history
     */

    @Override
    public PaymentHistoryDTO transferFund(FundTransferMetadata metadata, FundTransferRequestDTO request) {

        log.info("Starting fund transfer for {} ", request.getServiceType());

        log.info("Validating Financial Transaction number {} ", request.getFinTxnNo());
        validateFinTxnNo(request);

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

    private void validateFinTxnNo(FundTransferRequestDTO request) {
        if (paymentHistoryService.isFinancialTransactionPresent(request.getFinTxnNo())) {
            GenericExceptionHandler.handleError(DUPLICATION_FUND_TRANSFER_REQUEST, DUPLICATION_FUND_TRANSFER_REQUEST.getErrorMessage());
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
