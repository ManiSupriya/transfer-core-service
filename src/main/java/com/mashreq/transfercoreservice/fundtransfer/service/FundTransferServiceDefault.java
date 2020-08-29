package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.webcore.dto.response.Response;
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
import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Slf4j
@TrackExec
@Service
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    private static final String FUND_TRANSFER_INITIATION_SUFFIX = "_FUND_TRANSFER_REQUEST";
    private final DigitalUserRepository digitalUserRepository;
    private final PaymentHistoryService paymentHistoryService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final OwnAccountStrategy ownAccountStrategy;
    private final WithinMashreqStrategy withinMashreqStrategy;
    private final LocalFundTransferStrategy localFundTransferStrategy;
    private final InternationalFundTransferStrategy internationalFundTransferStrategy;
    private final CharityStrategyDefault charityStrategyDefault;
    private final QuickRemitStrategy quickRemitStrategy;
    private final AsyncUserEventPublisher auditEventPublisher;
    private EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;
    private final OTPService otpService;


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
        fundTransferStrategies.put(QUICK_REMIT, quickRemitStrategy);
    }

    @Override
    public FundTransferResponseDTO transferFund(RequestMetaData metadata, FundTransferRequestDTO request) {
        final ServiceType serviceType = getServiceByType(request.getServiceType());
        final FundTransferEventType initiatedEvent = FundTransferEventType.getEventTypeByCode(serviceType.getEventPrefix() + FUND_TRANSFER_INITIATION_SUFFIX);
        if(!OWN_ACCOUNT.getName().equals(serviceType.getName())){
            verifyOtp(request,metadata);
        }
        return auditEventPublisher.publishEventLifecycle(
                () -> getFundTransferResponse(metadata, request),
                initiatedEvent,
                metadata,
                getInitiatedRemarks(request));
    }

    private void verifyOtp(FundTransferRequestDTO request, RequestMetaData metadata) {
        VerifyOTPRequestDTO verifyOTPRequestDTO = new VerifyOTPRequestDTO();
        verifyOTPRequestDTO.setOtp(request.getOtp());
        verifyOTPRequestDTO.setChallengeToken(request.getChallengeToken());
        verifyOTPRequestDTO.setDpPublicKeyIndex(request.getDpPublicKeyIndex());
        verifyOTPRequestDTO.setDpRandomNumber(request.getDpRandomNumber());
        verifyOTPRequestDTO.setLoginId(metadata.getLoginId());
        verifyOTPRequestDTO.setRedisKey(metadata.getUserCacheKey());
        log.info("fund transfer otp request{} ", verifyOTPRequestDTO);
        Response<VerifyOTPResponseDTO> verifyOTP = otpService.verifyOTP(verifyOTPRequestDTO);
        log.info("fund transfer otp response{} ", htmlEscape(verifyOTP.getStatus().toString()));
        if (!verifyOTP.getData().isAuthenticated()) {
            auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.FUND_TRANSFER_OTP_DOES_NOT_MATCH,
                    metadata, CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
                    TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.toString(),
                    TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage());
            GenericExceptionHandler.handleError(TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR,
                    verifyOTP.getErrorDetails(), verifyOTP.getErrorDetails());
        }
        auditEventPublisher.publishSuccessEvent(FundTransferEventType.FUND_TRANSFER_OTP_VALIDATION, metadata, FundTransferEventType.FUND_TRANSFER_OTP_VALIDATION.getDescription());

    }

    private FundTransferResponseDTO getFundTransferResponse(RequestMetaData metadata, FundTransferRequestDTO request) {
        Instant start = now();
        log.info("Starting fund transfer for {} ", htmlEscape(request.getServiceType()));
        log.info("Finding Digital User for CIF-ID {}", htmlEscape(metadata.getPrimaryCif()));
        DigitalUser digitalUser = getDigitalUser(metadata);

        log.info("Creating  User DTO");
        UserDTO userDTO = createUserDTO(metadata, digitalUser);

        FundTransferStrategy strategy = fundTransferStrategies.get(getServiceByType(request.getServiceType()));
        FundTransferResponse response = strategy.execute(request, metadata, userDTO);


        if (isSuccessOrProcessing(response)) {
            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(
                    request.getServiceType(), response.getLimitUsageAmount(), userDTO, metadata, response.getLimitVersionUuid(),response.getTransactionRefNo());
            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
            digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
        }

        // Insert payment history irrespective of mw payment fails or success
        PaymentHistoryDTO paymentHistoryDTO = generatePaymentHistory(request, response, userDTO, metadata);

        log.info("Inserting into Payments History table {} ", paymentHistoryDTO);
        paymentHistoryService.insert(paymentHistoryDTO);

        log.info("Total time taken for {} Fund Transfer {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));

        if (isFailure(response)) {
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
                .transactionRefNo(response.getTransactionRefNo())
                .build();
    }

    private boolean isFailure(FundTransferResponse response) {
        return MwResponseStatus.F.equals(response.getResponseDto().getMwResponseStatus());
    }

    private boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }

    private String getFailureMessage(TransferErrorCode fundTransferFailed, FundTransferRequestDTO request, FundTransferResponse response) {
        return String.format("FIN-TXN-NO [%s] : REFERENCE-NO [%s] REFERENCE-MESSAGE [%s] : ",
                request.getFinTxnNo(),
                response.getResponseDto().getMwReferenceNo(),
                fundTransferFailed.getErrorMessage()
        );
    }

    private String getInitiatedRemarks(FundTransferRequestDTO request) {
        return String.format("From Account = %s, To Account = %s, Amount = %s, Currency = %s, Financial Transaction Number = %s, Beneficiary Id = %s ",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getCurrency(),
                request.getFinTxnNo(),
                request.getBeneficiaryId());
    }

    private DigitalUser getDigitalUser(RequestMetaData fundTransferMetadata) {
        Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
        if (!digitalUserOptional.isPresent()) {
            GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
        }
        log.info("Digital User found successfully {} ", digitalUserOptional.get());

        return digitalUserOptional.get();
    }

    private UserDTO createUserDTO(RequestMetaData fundTransferMetadata, DigitalUser digitalUser) {
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
                                                            RequestMetaData fundTransferMetadata, String versionUuid,String transactionRefNo) {
        return DigitalUserLimitUsageDTO.builder()
                .digitalUserId(userDTO.getUserId())
                .cif(fundTransferMetadata.getPrimaryCif())
                .channel(fundTransferMetadata.getChannel())
                .beneficiaryTypeCode(serviceType)
                .paidAmount(usageAmount)
                .versionUuid(versionUuid)
                .createdBy(String.valueOf(userDTO.getUserId()))
                .transactionRefNo(transactionRefNo)
                .build();

    }

    PaymentHistoryDTO generatePaymentHistory(FundTransferRequestDTO request, FundTransferResponse fundTransferResponse, UserDTO userDTO,
                                             RequestMetaData fundTransferMetadata) {

        //convert dto
        return PaymentHistoryDTO.builder()
                .cif(fundTransferMetadata.getPrimaryCif())
                .userId(userDTO.getUserId())
                .accountTo(request.getToAccount())
                .beneficiaryTypeCode(request.getServiceType())
                .channel(fundTransferMetadata.getChannel())
                //.billRefNo(coreResponse.getBillRefNo())
                .ipAddress(fundTransferMetadata.getDeviceIP())
                .paidAmount(request.getAmount() == null ? request.getSrcAmount() : request.getAmount())
                //.dueAmount(request.getDueAmount())
                //.toCurrency(PaymentConstants.BILL_PAYMENT_TO_CURRENCY)
                .status(fundTransferResponse.getResponseDto().getMwResponseStatus().name())
                .mwReferenceNo(fundTransferResponse.getResponseDto().getMwReferenceNo())
                .mwResponseCode(fundTransferResponse.getResponseDto().getMwResponseCode())
                .mwResponseDescription(fundTransferResponse.getResponseDto().getMwResponseDescription())
                .accountFrom(request.getFromAccount())
                .financialTransactionNo(request.getFinTxnNo())
                .transactionRefNo(fundTransferResponse.getTransactionRefNo())
                .hostRefNo(fundTransferResponse.getResponseDto().getHostRefNo())
                .build();

    }
}
