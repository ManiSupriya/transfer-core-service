package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_SERVICE_CONNECTION_ERROR;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@TrackExec
@Slf4j
@RequiredArgsConstructor
public class AccountCardLessCashBlockRequestService implements CoreEnquiryService<CardLessCashBlockResponse, CardLessCashBlockRequest>{
	 private final AccountClient accountClient;
	 private final ExternalErrorCodeConfig errorCodeConfig;
	 public Response<CardLessCashBlockResponse> blockCardLessCashRequest(CardLessCashBlockRequest blockRequest) {
		 return accountClient.blockCardLessCashRequest(blockRequest);
	}
	@Override
	public Response<CardLessCashBlockResponse> doCall(CardLessCashBlockRequest blockRequest) {
		return accountClient.blockCardLessCashRequest(blockRequest);
	}
	@Override
	public Map<String, String> errorMap() {
		 return errorCodeConfig.getAccountDetailsExternalErrorCodesMap();
	}
	@Override
	public Response<CardLessCashBlockResponse> defaultSuccessResponse() {
		return new Response<CardLessCashBlockResponse>();
	}
	@Override
	public TransferErrorCode assignDefaultErrorCode() {
		 return ACC_EXTERNAL_SERVICE_ERROR;
	}
	@Override
	public TransferErrorCode assignFeignConnectionErrorCode() {
		 return ACC_SERVICE_CONNECTION_ERROR;
	}
	@Override
	public void assignCustomErrorCode(String errorDetail, TransferErrorCode errorCode) {
		GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage());
	}
}
