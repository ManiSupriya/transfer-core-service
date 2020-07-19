package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import java.util.List;

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
import com.mashreq.transfercoreservice.common.OTPExternalConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import static com.mashreq.transfercoreservice.common.CommonConstants.*;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;

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
	private final OTPExternalConfig otpConfig;

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
		verifyOTPRequestDTO.setDpPublicKeyIndex(otpConfig.getDpPublicKey());
		verifyOTPRequestDTO.setDpRandomNumber(otpConfig.getDpRandomNumber());
		verifyOTPRequestDTO.setLoginId(userId);
		verifyOTPRequestDTO.setRedisKey(metaData.getUserCacheKey());
		log.info("cardLessCash Generation otp request{} ", verifyOTPRequestDTO);
		try {
		Response<VerifyOTPResponseDTO> verifyOTP = otpService.verifyOTP(verifyOTPRequestDTO);
		log.info("cardLessCash Generation otp response{} ", verifyOTP);
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
			generatePaymentHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userId);
			throw exception;

		} catch (Exception exception) {
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.CARD_LESS_CASH_GENERATION_FAILED,
					metaData, CARD_LESS_CASH, metaData.getChannelTraceId(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.toString(),
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage(), exception.getMessage());
			generatePaymentHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userId);
			GenericExceptionHandler.handleError(TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR,
					TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR.getErrorMessage());

		}

		/**
		 * Insert payment history irrespective of payment fails or success
		 */
		generatePaymentHistory(cardLessCashGenerationRequest, cardLessCashGenerationResponse, metaData, userId);

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
			Response<CardLessCashGenerationResponse> coreResponse, RequestMetaData metaData, String userId) {
		
		PaymentHistoryDTO paymentHistoryDTO = PaymentHistoryDTO.builder().cif(metaData.getPrimaryCif()).userId(Long.parseLong(userId)).channel(MOB_CHANNEL)
				.beneficiaryTypeCode(CARD_LESS_CASH).paidAmount(cardLessCashGenerationRequest.getAmount())
				.status(coreResponse.getStatus().toString()).ipAddress(metaData.getDeviceIP())
				.mwReferenceNo(coreResponse.getData().getReferenceNumber())
				.mwResponseCode(coreResponse.getErrorCode()).mwResponseDescription(coreResponse.getErrorDetails())
				.accountFrom(cardLessCashGenerationRequest.getAccountNo()).accountTo(cardLessCashGenerationRequest.getAccountNo())
				.financialTransactionNo(coreResponse.getData().getReferenceNumber()).build();
		
		log.info("Inserting into Payments History table {} ", paymentHistoryDTO);
		paymentHistoryService.insert(paymentHistoryDTO);

	}

}
