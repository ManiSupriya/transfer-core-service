package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Optional;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.FUND_TRANSFER_FAILED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;
import static java.time.Duration.between;
import static java.time.Instant.now;

@Slf4j
@TrackExec
@Service
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    private final DigitalUserRepository digitalUserRepository;
    private final PaymentHistoryService paymentHistoryService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final OwnAccountStrategy ownAccountStrategy;
    private final WithinMashreqStrategy withinMashreqStrategy;
    private final LocalFundTransferStrategy localFundTransferStrategy;
    private final InternationalFundTransferStrategy internationalFundTransferStrategy;
    private final CharityStrategyDefault charityStrategyDefault;
    private final QuickRemitStrategy quickRemitStrategy;
    private EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;



    @PostConstruct
    public void init() {
        fundTransferStrategies = new EnumMap<>(ServiceType.class);
        fundTransferStrategies.put(OWN_ACCOUNT, ownAccountStrategy);
        fundTransferStrategies.put(WITHIN_MASHREQ, withinMashreqStrategy);
        fundTransferStrategies.put(LOCAL, localFundTransferStrategy);
        fundTransferStrategies.put(INTERNATIONAL, internationalFundTransferStrategy);
        fundTransferStrategies.put(BAIT_AL_KHAIR, charityStrategyDefault);
        fundTransferStrategies.put(DUBAI_CARE, charityStrategyDefault);
        fundTransferStrategies.put(DAR_AL_BER, charityStrategyDefault);
        fundTransferStrategies.put(QUICK_REMIT,quickRemitStrategy);

    }

    @Override
    public FundTransferResponseDTO transferFund(FundTransferMetadata metadata, FundTransferRequestDTO request) {
        Instant start = now();
        log.info("Starting fund transfer for {} ", request.getServiceType());

        log.info("Finding Digital User for CIF-ID {}", metadata.getPrimaryCif());
        DigitalUser digitalUser = getDigitalUser(metadata);

        log.info("Creating  User DTO");
        UserDTO userDTO = createUserDTO(metadata, digitalUser);

        FundTransferStrategy strategy = fundTransferStrategies.get(ServiceType.getServiceByType(request.getServiceType()));
        FundTransferResponse response = strategy.execute(request, metadata, userDTO);


        if (response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S)) {

            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(
                    request.getServiceType(), response.getLimitUsageAmount(), userDTO, metadata, response.getLimitVersionUuid());
            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
            digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
        }

        // Insert payment history irrespective of mw payment fails or success
        PaymentHistoryDTO paymentHistoryDTO = generatePaymentHistory(request, response.getResponseDto(), userDTO, metadata);

        log.info("Inserting into Payments History table {} ", paymentHistoryDTO);
        paymentHistoryService.insert(paymentHistoryDTO);

        log.info("Total time taken for {} Fund Transfer {} milli seconds ", request.getServiceType(), between(start, now()).toMillis());

        if (MwResponseStatus.F.equals(response.getResponseDto().getMwResponseStatus())) {
            GenericExceptionHandler.handleError(FUND_TRANSFER_FAILED,
                    getFailureMessage(FUND_TRANSFER_FAILED, request, response),
                    response.getResponseDto().getMwResponseCode());
        }
        return FundTransferResponseDTO.builder()
                .accountTo(paymentHistoryDTO.getAccountTo())
                .status(paymentHistoryDTO.getStatus())
                .paidAmount(paymentHistoryDTO.getPaidAmount())
                .mwReferenceNo(paymentHistoryDTO.getMwReferenceNo())
                .mwResponseCode(paymentHistoryDTO.getMwResponseCode())
                .mwResponseDescription(paymentHistoryDTO.getMwResponseDescription())
                .financialTransactionNo(request.getFinTxnNo())
                .build();
    }

    private String getFailureMessage(TransferErrorCode fundTransferFailed, FundTransferRequestDTO request, FundTransferResponse response) {
        return String.format("FIN-TXN-NO [%s] : REFERENCE-NO [%s] REFERENCE-MESSAGE [%s] : ",
                request.getFinTxnNo(),
                response.getResponseDto().getMwReferenceNo(),
                fundTransferFailed.getErrorMessage()
        );
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
        userDTO.setLocalCurrency(digitalUser.getDigitalUserGroup().getCountry().getLocalCurrency());

        log.info("User DTO  created {} ", userDTO);
        return userDTO;
    }

    private DigitalUserLimitUsageDTO generateUserLimitUsage(String serviceType, BigDecimal usageAmount, UserDTO userDTO,
                                                            FundTransferMetadata fundTransferMetadata, String versionUuid) {
        return DigitalUserLimitUsageDTO.builder()
                .digitalUserId(userDTO.getUserId())
                .cif(fundTransferMetadata.getPrimaryCif())
                .channel(fundTransferMetadata.getChannel())
                .beneficiaryTypeCode(serviceType)
                .paidAmount(usageAmount)
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
