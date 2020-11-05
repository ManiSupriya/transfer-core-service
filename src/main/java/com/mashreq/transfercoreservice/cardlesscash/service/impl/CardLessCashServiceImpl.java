package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import static com.mashreq.transfercoreservice.common.CommonConstants.CARD_LESS_CASH;
import static com.mashreq.transfercoreservice.common.CommonConstants.MOB_CHANNEL;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsage;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageRepository;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistory;
import com.mashreq.transfercoreservice.transactionqueue.TransactionRepository;
import com.mashreq.webcore.dto.response.Response;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class for the CardLessCashService
 */
@Slf4j
@Service
@AllArgsConstructor
public class CardLessCashServiceImpl implements CardLessCashService {
	private final AccountService accountService;
	private AsyncUserEventPublisher asyncUserEventPublisher;
	private final OTPService otpService;
	private final DigitalUserRepository digitalUserRepository;
	private final DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;
	private final BalanceValidator balanceValidator;
	private final LimitValidator limitValidator;
	private final TransactionRepository transactionRepository;

	@Override
	public Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest,
			RequestMetaData metaData) {
		CardLessCashQueryRequest cardLessCashQueryRequest = new CardLessCashQueryRequest();
		cardLessCashQueryRequest.setAccountNumber(blockRequest.getAccountNumber());
		cardLessCashQueryRequest.setRemitNumDays(1);
		List<CardLessCashQueryResponse> cardLessCashQueryResponse = accountService.cardLessCashRemitQuery(cardLessCashQueryRequest, metaData).getData();
		boolean isRefNOValid = false;
		for(CardLessCashQueryResponse queryResponse:cardLessCashQueryResponse)
		{
			if(queryResponse.getRemitNo().equalsIgnoreCase(blockRequest.getReferenceNumber()));
			isRefNOValid = true;
		}
		if(!isRefNOValid) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_REFERENCE_NO_INVALID,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.REFERENCE_NO_INVALID.toString(),
					TransferErrorCode.REFERENCE_NO_INVALID.getErrorMessage(),
					TransferErrorCode.REFERENCE_NO_INVALID.getErrorMessage());
			GenericExceptionHandler.handleError(TransferErrorCode.REFERENCE_NO_INVALID,
					TransferErrorCode.REFERENCE_NO_INVALID.getErrorMessage(), TransferErrorCode.REFERENCE_NO_INVALID.getErrorMessage());
		}
		return accountService.blockCardLessCashRequest(blockRequest, metaData);
	}

	@Override
	public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(
			CardLessCashGenerationRequest cardLessCashGenerationRequest, String userMobileNumber, String userId,
			RequestMetaData metaData) {
		log.info("cardLessCash GenerationRequest {} ", cardLessCashGenerationRequest);
		Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = null;
		VerifyOTPRequestDTO verifyOTPRequestDTO = new VerifyOTPRequestDTO();
		verifyOTPRequestDTO.setOtp(cardLessCashGenerationRequest.getOtp());
		verifyOTPRequestDTO.setChallengeToken(cardLessCashGenerationRequest.getChallengeToken());
		verifyOTPRequestDTO.setDpPublicKeyIndex(cardLessCashGenerationRequest.getDpPublicKeyIndex());
		verifyOTPRequestDTO.setDpRandomNumber(cardLessCashGenerationRequest.getDpRandomNumber());
		verifyOTPRequestDTO.setLoginId(userId);
		verifyOTPRequestDTO.setRedisKey(metaData.getUserCacheKey());
		log.info("cardLessCash Generation otp request{} ", verifyOTPRequestDTO);
			Response<VerifyOTPResponseDTO> verifyOTP = otpService.verifyOTP(verifyOTPRequestDTO);
			log.info("cardLessCash Generation otp response{} ", htmlEscape(verifyOTP.getStatus()));
			if (!verifyOTP.getData().isAuthenticated()) {
				asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_OTP_DOES_NOT_MATCH,
						metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
						TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.toString(),
						TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
						TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR,
						verifyOTP.getErrorDetails(), verifyOTP.getErrorDetails());
			}
		asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.CARD_LESS_CASH_OTP_VALIDATION, metaData, FundTransferEventType.CARD_LESS_CASH_OTP_VALIDATION.getDescription());
		DigitalUser digitalUser = getDigitalUser(metaData);

		log.info("Creating  User DTO");
		UserDTO userDTO = createUserDTO(metaData, digitalUser);
		 if (!balanceValidator.validateBalance(cardLessCashGenerationRequest, metaData)) {
			 asyncUserEventPublisher.publishFailureEvent(FundTransferEventType.CARD_LESS_CASH_BALANCE_VALIDATION, metaData, FundTransferEventType.CARD_LESS_CASH_BALANCE_VALIDATION.name(), TransferErrorCode.BALANCE_NOT_SUFFICIENT.name(), TransferErrorCode.BALANCE_NOT_SUFFICIENT.name(), TransferErrorCode.BALANCE_NOT_SUFFICIENT.getErrorMessage());
	            GenericExceptionHandler.handleError(TransferErrorCode.BALANCE_NOT_SUFFICIENT, TransferErrorCode.BALANCE_NOT_SUFFICIENT.getErrorMessage());
	        }
		 asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.CARD_LESS_CASH_BALANCE_VALIDATION_SUCCESS, metaData, FundTransferEventType.CARD_LESS_CASH_BALANCE_VALIDATION_SUCCESS.getDescription());
		 limitValidator.validateWithProc(userDTO, CommonConstants.CARD_LESS_CASH, cardLessCashGenerationRequest.getAmount(), metaData, null);  
		 asyncUserEventPublisher.publishSuccessEvent(FundTransferEventType.LIMIT_CHECK_SUCCESS, metaData, FundTransferEventType.LIMIT_CHECK_SUCCESS.getDescription());
			cardLessCashGenerationResponse = accountService
					.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, userMobileNumber, metaData);
			insertUserLimitUsage(metaData, cardLessCashGenerationRequest.getAmount());
		/**
		 * Insert transaction history irrespective of payment fails or success
		 */
		generateTransactionHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userDTO);

		return cardLessCashGenerationResponse;
	}

	@Override
	public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(
			CardLessCashQueryRequest cardLessCashQueryRequest, RequestMetaData metaData) {
		log.info("cardLessCash  Query Details {} ", cardLessCashQueryRequest);
		return accountService.cardLessCashRemitQuery(cardLessCashQueryRequest, metaData);
	}
	
	private void generateTransactionHistory(CardLessCashGenerationRequest cardLessCashGenerationRequest,
			Response<CardLessCashGenerationResponse> coreResponse, RequestMetaData metaData, UserDTO userDTO) {
		TransactionHistory transactionHistory = TransactionHistory.builder().cif(metaData.getPrimaryCif())
				.userId(userDTO.getUserId()).channel(MOB_CHANNEL)
				.transactionTypeCode(CARD_LESS_CASH)
				.paidAmount(cardLessCashGenerationRequest.getAmount())
				.status(coreResponse.getStatus().toString())
				.ipAddress(metaData.getDeviceIP())
				.mwResponseDescription(coreResponse.getErrorDetails())
				.accountFrom(cardLessCashGenerationRequest.getAccountNo())
				.accountTo(cardLessCashGenerationRequest.getAccountNo())
				.billRefNo(coreResponse.getData().getReferenceNumber())
				.valueDate(LocalDateTime.now())
				.transactionRefNo(coreResponse.getData().getReferenceNumber())
				.financialTransactionNo(coreResponse.getData().getReferenceNumber()).build();
		log.info("Inserting into Transaction History table {} ", htmlEscape(transactionHistory.getTransactionRefNo()));
		transactionRepository.save(transactionHistory);

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

	private DigitalUser getDigitalUser(RequestMetaData metaData) {
		Optional<DigitalUser> digitalUserOptional = digitalUserRepository.findByCifEquals(metaData.getPrimaryCif());
		if (!digitalUserOptional.isPresent()) {
			GenericExceptionHandler.handleError(INVALID_CIF, INVALID_CIF.getErrorMessage());
		}
		log.info("Digital User found successfully {} ", digitalUserOptional.get());

		return digitalUserOptional.get();
	}
	
	public void insertUserLimitUsage(RequestMetaData metaData, BigDecimal amount) {
        DigitalUserLimitUsage digitalUserLimitUsage = new DigitalUserLimitUsage();
        DigitalUser digitalUser = getDigitalUser(metaData);
        Long userId = digitalUser.getId();
        digitalUserLimitUsage.setDigitalUserId(userId);
        digitalUserLimitUsage.setBeneficiaryTypeCode(CARD_LESS_CASH);
        digitalUserLimitUsage.setCif(digitalUser.getCif());
        digitalUserLimitUsage.setChannel(metaData.getChannel());
        digitalUserLimitUsage.setPaidAmount(amount);
        digitalUserLimitUsage.setCreatedBy(String.valueOf(userId));
        System.out.println(digitalUserLimitUsage.getCif()+"cif ID-"+digitalUserLimitUsage.getDigitalUserId());
       digitalUserLimitUsage.setVersionUuid("DEF-1");
        log.info("Store limit usage for CIF={} and beneficiaryTypeCode={} ",
                htmlEscape(digitalUserLimitUsage.getCif()), htmlEscape(String.valueOf(digitalUserLimitUsage.getDigitalUserId())));
        digitalUserLimitUsageRepository.save(digitalUserLimitUsage);
    }

}
