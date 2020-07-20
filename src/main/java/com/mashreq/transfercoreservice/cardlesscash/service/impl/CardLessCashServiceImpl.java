package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import static com.mashreq.transfercoreservice.common.CommonConstants.*;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CIF;

import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

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
    private final PaymentHistoryService paymentHistoryService;
    private final OTPService otpService;
    private final DigitalUserRepository digitalUserRepository;

    @Override
	public Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest,
			RequestMetaData metaData) {

		Response<CardLessCashBlockResponse> cardLessCashBlockResponse = null;
		try {
			cardLessCashBlockResponse = accountService.blockCardLessCashRequest(blockRequest);
			asyncUserEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.CARD_LESS_CASH_BLOCK_REQUEST,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId());

		} catch (GenericException exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_BLOCK_REQUEST_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(), exception.getErrorCode(),
					exception.getMessage(), exception.getErrorDetails());
			throw exception;

		} catch (Exception exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_BLOCK_REQUEST_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.toString(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), exception.getMessage());
			GenericExceptionHandler.handleError(TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR,
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage());

		}
		return cardLessCashBlockResponse;
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
		try {
		Response<VerifyOTPResponseDTO> verifyOTP = otpService.verifyOTP(verifyOTPRequestDTO);
		log.info("cardLessCash Generation otp response{} ", htmlEscape(verifyOTP.getStatus().toString()));
			if (!verifyOTP.getData().isAuthenticated()) {
				asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_OTP_DOES_NOT_MATCH,
						metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
						TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.toString(),
						TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
						TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR,
						verifyOTP.getErrorDetails(), verifyOTP.getErrorDetails());
			}
		} catch (GenericException ge) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_OTP_DOES_NOT_MATCH,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.toString(),
					TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage(),
					TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR.getErrorMessage());
			GenericExceptionHandler.handleError(TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR,
					ge.getErrorDetails(), ge.getErrorDetails());
		}
		 DigitalUser digitalUser = getDigitalUser(metaData);

	        log.info("Creating  User DTO");
	        UserDTO userDTO = createUserDTO(metaData, digitalUser);
		try {
			cardLessCashGenerationResponse = accountService
					.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, userMobileNumber);
			asyncUserEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_REQUEST,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId());

		} catch (GenericException exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(), exception.getErrorCode(),
					exception.getMessage(), exception.getErrorDetails());
			/**
			 * Insert payment history irrespective of payment fails or success
			 */
			generatePaymentHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userDTO);
			throw exception;

		} catch (Exception exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.toString(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), exception.getMessage());
			generatePaymentHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userDTO);
			GenericExceptionHandler.handleError(TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR,
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage());

		}

		/**
		 * Insert payment history irrespective of payment fails or success
		 */
		generatePaymentHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userDTO);

		return cardLessCashGenerationResponse;
	}
		
	@Override
	public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery(
			CardLessCashQueryRequest cardLessCashQueryRequest, RequestMetaData metaData) {
		Response<List<CardLessCashQueryResponse>> cardLessCashQueryResponse = null;
		log.info("cardLessCash  Query Details {} ", cardLessCashQueryRequest);
		try {
			cardLessCashQueryResponse = accountService.cardLessCashRemitQuery(cardLessCashQueryRequest);
			asyncUserEventPublisher.publishSuccessfulEsbEvent(FundTransferEventType.CARD_LESS_CASH_QUERY_DETAILS,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId());

		} catch (GenericException exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_QUERY_DETAILS_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(), exception.getErrorCode(),
					exception.getMessage(), exception.getErrorDetails());
			throw exception;

		} catch (Exception exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_QUERY_DETAILS_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.toString(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), exception.getMessage());
			GenericExceptionHandler.handleError(TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR,
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage());

		}
		return cardLessCashQueryResponse;
	}
	
	private void generatePaymentHistory(CardLessCashGenerationRequest cardLessCashGenerationRequest,
			Response<CardLessCashGenerationResponse> coreResponse, RequestMetaData metaData, UserDTO userDTO) {
		
		PaymentHistoryDTO paymentHistoryDTO = PaymentHistoryDTO.builder().cif(metaData.getPrimaryCif()).userId(userDTO.getUserId()).channel(MOB_CHANNEL)
				.beneficiaryTypeCode(CARD_LESS_CASH).paidAmount(cardLessCashGenerationRequest.getAmount())
				.status(coreResponse.getStatus().toString()).ipAddress(metaData.getDeviceIP())
				.mwReferenceNo(coreResponse.getData().getReferenceNumber())
				.mwResponseCode(coreResponse.getErrorCode()).mwResponseDescription(coreResponse.getErrorDetails())
				.accountFrom(cardLessCashGenerationRequest.getAccountNo()).accountTo(cardLessCashGenerationRequest.getAccountNo())
				.financialTransactionNo(coreResponse.getData().getReferenceNumber()).build();
		
		log.info("Inserting into Payments History table {} ", paymentHistoryDTO);
		paymentHistoryService.insert(paymentHistoryDTO);

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
	
}
