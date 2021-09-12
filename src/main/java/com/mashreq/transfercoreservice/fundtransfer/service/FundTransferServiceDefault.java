package com.mashreq.transfercoreservice.fundtransfer.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.BAIT_AL_KHAIR;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.DAR_AL_BER;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.DUBAI_CARE;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.getServiceByType;
import static java.time.Duration.between;
import static java.time.Instant.now;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.CharityStrategyDefault;
import com.mashreq.transfercoreservice.fundtransfer.strategy.FundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.InternationalFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.LocalFundTransferStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.OwnAccountStrategy;
import com.mashreq.transfercoreservice.fundtransfer.strategy.WithinMashreqStrategy;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.promo.service.PromoCodeService;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistory;
import com.mashreq.transfercoreservice.transactionqueue.TransactionRepository;
import com.mashreq.webcore.dto.response.Response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TrackExec
@Service
@Getter
@Primary
@RequiredArgsConstructor
public class FundTransferServiceDefault implements FundTransferService {

    private static final String FUND_TRANSFER_INITIATION_SUFFIX = "_FUND_TRANSFER_REQUEST";
    private final DigitalUserRepository digitalUserRepository;
    private final TransactionRepository transactionRepository;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    private final OwnAccountStrategy ownAccountStrategy;
    private final WithinMashreqStrategy withinMashreqStrategy;
    private final LocalFundTransferStrategy localFundTransferStrategy;
    private final InternationalFundTransferStrategy internationalFundTransferStrategy;
    private final CharityStrategyDefault charityStrategyDefault;
    private final AsyncUserEventPublisher auditEventPublisher;
    protected EnumMap<ServiceType, FundTransferStrategy> fundTransferStrategies;
    private final OTPService otpService;
    private final ExternalErrorCodeConfig errorCodeConfig;
    private final PromoCodeService promoCodeService;
    private final MobCommonService mobCommonService;
    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${app.nonProd.otpRelaxed}")
    private boolean otpRelaxed;

    @PostConstruct
    public void init() {
        fundTransferStrategies = new EnumMap<>(ServiceType.class);
        fundTransferStrategies.put(WYMA, ownAccountStrategy);
        fundTransferStrategies.put(WAMA, withinMashreqStrategy);
        fundTransferStrategies.put(LOCAL, localFundTransferStrategy);
        fundTransferStrategies.put(INFT, internationalFundTransferStrategy);
        fundTransferStrategies.put(BAIT_AL_KHAIR, charityStrategyDefault);
        fundTransferStrategies.put(DUBAI_CARE, charityStrategyDefault);
        fundTransferStrategies.put(DAR_AL_BER, charityStrategyDefault);
    }

    @Override
    public FundTransferResponseDTO transferFund(RequestMetaData metadata, FundTransferRequestDTO request) {
        final ServiceType serviceType = getServiceByType(request.getServiceType());
        final FundTransferEventType initiatedEvent = FundTransferEventType.getEventTypeByCode(serviceType.getEventPrefix() + FUND_TRANSFER_INITIATION_SUFFIX);
        if(!WYMA.getName().equals(serviceType.getName())){
            verifyOtp(request,metadata);
        }
        return auditEventPublisher.publishEventLifecycle(
                () -> getFundTransferResponse(metadata, request),
                initiatedEvent,
                metadata,
                getInitiatedRemarks(request));
    }

    protected void verifyOtp(FundTransferRequestDTO request, RequestMetaData metadata) {
    	if(!CommonConstants.PROD_PROFILE.equals(activeProfile) && otpRelaxed) {
    		log.info("OTP relaxed for environment {}",activeProfile);
    		return;
    	}
        VerifyOTPRequestDTO verifyOTPRequestDTO = new VerifyOTPRequestDTO();
        verifyOTPRequestDTO.setOtp(request.getOtp());
        verifyOTPRequestDTO.setChallengeToken(request.getChallengeToken());
        verifyOTPRequestDTO.setDpPublicKeyIndex(request.getDpPublicKeyIndex());
        verifyOTPRequestDTO.setDpRandomNumber(request.getDpRandomNumber());
        verifyOTPRequestDTO.setLoginId(metadata.getLoginId());
        verifyOTPRequestDTO.setRedisKey(metadata.getUserCacheKey());
        log.info("fund transfer otp request{} ", verifyOTPRequestDTO);
        Response<VerifyOTPResponseDTO> verifyOTP = otpService.verifyOTP(verifyOTPRequestDTO);
        log.info("fund transfer otp response{} ", htmlEscape(verifyOTP.getStatus()));
        if (ObjectUtils.isEmpty(verifyOTP.getData()) || !verifyOTP.getData().isAuthenticated()) {
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

    protected FundTransferResponseDTO getFundTransferResponse(RequestMetaData metadata, FundTransferRequestDTO request) {
        Instant start = now();
        log.info("Starting fund transfer for {} ", htmlEscape(request.getServiceType()));
        log.info("Finding Digital User for CIF-ID {}", htmlEscape(metadata.getPrimaryCif()));
        DigitalUser digitalUser = getDigitalUser(metadata);

        log.info("Creating  User DTO");
        UserDTO userDTO = createUserDTO(metadata, digitalUser);

        //deal number not applicable if both currencies are same
        if(StringUtils.isNotBlank(request.getTxnCurrency()) && request.getCurrency().equalsIgnoreCase(request.getTxnCurrency()) && (request.getDealNumber()!=null && !request.getDealNumber().isEmpty())) {
        	 auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.DEAL_VALIDATION,
                     metadata, CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
                     TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.toString(),
                     TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage(),
                     TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage());
        	 GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY,
        			  TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage(), TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY.getErrorMessage());
        }
        
        FundTransferStrategy strategy = fundTransferStrategies.get(getServiceByType(request.getServiceType()));
        FundTransferResponse response = strategy.execute(request, metadata, userDTO);


        handleIfTransactionIsSuccess(metadata, request, userDTO, response);

        TransactionHistory transactionHistory = updateTransactionHistory(metadata, request, userDTO, response);

        log.info("Total time taken for {} Fund Transfer {} milli seconds ", htmlEscape(request.getServiceType()), htmlEscape(Long.toString(between(start, now()).toMillis())));

        handleFailure(request, response);

        boolean promoApplied = promoCodeService.validateAndSave(request, transactionHistory.getStatus(), metadata);

        return FundTransferResponseDTO.builder()
                .accountTo(transactionHistory.getAccountTo())
                .status(transactionHistory.getStatus())
                .paidAmount(transactionHistory.getPaidAmount())
                .mwReferenceNo(transactionHistory.getBillRefNo())
                .mwResponseCode(transactionHistory.getMwResponseCode())
                .mwResponseDescription(transactionHistory.getMwResponseDescription())
                .transactionRefNo(response.getTransactionRefNo())
                .promoCode(request.getPromoCode())
                .promoApplied(promoApplied)
                .build();
    }

    protected void handleIfTransactionIsSuccess(RequestMetaData metadata, FundTransferRequestDTO request,
			UserDTO userDTO, FundTransferResponse response) {
		if (isSuccessOrProcessing(response)) {
        	Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId())?Long.parseLong(request.getBeneficiaryId()):null;
            DigitalUserLimitUsageDTO digitalUserLimitUsageDTO = generateUserLimitUsage(
                    request.getServiceType(), response.getLimitUsageAmount(), userDTO, metadata, response.getLimitVersionUuid(),response.getTransactionRefNo(), bendId );
            log.info("Inserting into limits table {} ", digitalUserLimitUsageDTO);
            digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
        }
	}

	protected void handleFailure(FundTransferRequestDTO request, FundTransferResponse response) {
		if (isFailure(response)) {
            GenericExceptionHandler.handleError(TransferErrorCode.valueOf(errorCodeConfig.getMiddlewareExternalErrorCodesMap().getOrDefault(response.getResponseDto().getMwResponseCode(),"FUND_TRANSFER_FAILED")),
                    getFailureMessage(TransferErrorCode.valueOf(errorCodeConfig.getMiddlewareExternalErrorCodesMap().getOrDefault(response.getResponseDto().getMwResponseCode(),"FUND_TRANSFER_FAILED")), request, response),
                    response.getResponseDto().getMwResponseCode()+"-"+ response.getResponseDto().getMwResponseDescription());
        }
	}

	protected TransactionHistory updateTransactionHistory(RequestMetaData metadata, FundTransferRequestDTO request,
			UserDTO userDTO, FundTransferResponse response) {
		/** Insert payment history irrespective of mw payment fails or success */
        TransactionHistory transactionHistory = generateTransactionHistory(request, response, userDTO, metadata);
        log.info("Inserting into Payments History table {} ", htmlEscape(transactionHistory.getTransactionRefNo()));
        updateExchangeRate(transactionHistory);
        transactionRepository.save(transactionHistory);
		return transactionHistory;
	}

    private void updateExchangeRate(TransactionHistory transactionHistory) {
		if(NumberUtils.isCreatable(transactionHistory.getDebitAmount()) && transactionHistory.getPaidAmount()!= null) {
			BigDecimal debitAmnt = new BigDecimal(transactionHistory.getDebitAmount());
			transactionHistory.setExchangeRate(debitAmnt.divide(transactionHistory.getPaidAmount(),3, RoundingMode.HALF_DOWN).toPlainString());
		}
	}

	protected boolean isFailure(FundTransferResponse response) {
        return MwResponseStatus.F.equals(response.getResponseDto().getMwResponseStatus());
    }

    protected boolean isSuccessOrProcessing(FundTransferResponse response) {
        return response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.S) ||
                response.getResponseDto().getMwResponseStatus().equals(MwResponseStatus.P);
    }

    protected String getFailureMessage(TransferErrorCode fundTransferFailed, FundTransferRequestDTO request, FundTransferResponse response) {
        return String.format("FIN-TXN-NO [%s] : REFERENCE-NO [%s] REFERENCE-MESSAGE [%s] : ",
                request.getFinTxnNo(),
                response.getResponseDto().getMwReferenceNo(),
                fundTransferFailed.getErrorMessage()
        );
    }

    protected String getInitiatedRemarks(FundTransferRequestDTO request) {
        return String.format("From Account = %s, To Account = %s, Amount = %s, Currency = %s, Financial Transaction Number = %s, Beneficiary Id = %s ",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getCurrency(),
                request.getFinTxnNo(),
                request.getBeneficiaryId());
    }

    protected DigitalUser getDigitalUser(RequestMetaData fundTransferMetadata) {
        Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(fundTransferMetadata.getPrimaryCif());
        if (!digitalUserOptional.isPresent()) {
            GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
        }
        log.info("Digital User found successfully {} ", digitalUserOptional.get());

        return digitalUserOptional.get();
    }

    protected UserDTO createUserDTO(RequestMetaData fundTransferMetadata, DigitalUser digitalUser) {
        UserDTO userDTO = new UserDTO();
        userDTO.setCifId(fundTransferMetadata.getPrimaryCif());
        userDTO.setUserId(digitalUser.getId());
        userDTO.setSegmentId(digitalUser.getDigitalUserGroup().getSegment().getId());
        userDTO.setCountryId(digitalUser.getDigitalUserGroup().getCountry().getId());
        userDTO.setLocalCurrency(digitalUser.getDigitalUserGroup().getCountry().getLocalCurrency());
        userDTO.setDeviceRegisteredForPush(digitalUser.getDeviceidRegisteredForPushnotify());
        userDTO.setDeviceInfo(digitalUser.getDeviceInfo());
        log.info("User DTO  created {} ", userDTO);
        return userDTO;
    }

    protected DigitalUserLimitUsageDTO generateUserLimitUsage(String serviceType, BigDecimal usageAmount, UserDTO userDTO,
                                                            RequestMetaData fundTransferMetadata, String versionUuid,String transactionRefNo, Long benId) {
        return DigitalUserLimitUsageDTO.builder()
                .digitalUserId(userDTO.getUserId())
                .cif(fundTransferMetadata.getPrimaryCif())
                .channel(fundTransferMetadata.getChannel())
                .beneficiaryTypeCode(serviceType)
                .paidAmount(usageAmount)
                .versionUuid(versionUuid)
                .createdBy(String.valueOf(userDTO.getUserId()))
                .transactionRefNo(transactionRefNo)
                .beneficiaryId(benId)
                .build();

    }

    TransactionHistory generateTransactionHistory(FundTransferRequestDTO request, FundTransferResponse fundTransferResponse, UserDTO userDTO,
                                             RequestMetaData fundTransferMetadata) {
    	return TransactionHistory.builder()
                .cif(fundTransferMetadata.getPrimaryCif())
                .userId(userDTO.getUserId())
                .accountTo(request.getToAccount())
                .transactionTypeCode(request.getServiceType())
                .channel(fundTransferMetadata.getChannel())
                .billRefNo(fundTransferResponse.getResponseDto().getMwReferenceNo())
                .ipAddress(fundTransferMetadata.getDeviceIP())
                .paidAmount(request.getAmount() == null ? request.getSrcAmount() : request.getAmount())
                .fromCurrency(request.getCurrency())
                .toCurrency(request.getTxnCurrency())
                .status(fundTransferResponse.getResponseDto().getMwResponseStatus().getName())
                .mwResponseCode(fundTransferResponse.getResponseDto().getMwResponseCode())
                .mwResponseDescription(fundTransferResponse.getResponseDto().getMwResponseDescription())
                .accountFrom(request.getFromAccount())
                .financialTransactionNo(request.getFinTxnNo())
                .transactionRefNo(fundTransferResponse.getTransactionRefNo())
                .hostReferenceNo(fundTransferResponse.getResponseDto().getHostRefNo())
                .valueDate(LocalDateTime.now())
                .createdDate(Instant.now())
                .paymentNote(request.getPaymentNote())
                .chargeBearer(StringUtils.isNotEmpty(request.getChargeBearer())? ChargeBearer.valueOf(request.getChargeBearer()):null)
                .debitAmount(fundTransferResponse.getDebitAmount()!= null ? fundTransferResponse.getDebitAmount().toPlainString():null)
                .beneficiaryId(StringUtils.isNotBlank(request.getBeneficiaryId())?Long.valueOf(request.getBeneficiaryId()):null)
                .build();
    }
}
