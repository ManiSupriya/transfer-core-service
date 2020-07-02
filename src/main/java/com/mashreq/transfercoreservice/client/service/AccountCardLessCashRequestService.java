package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_SERVICE_CONNECTION_ERROR;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenReq;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
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
public class AccountCardLessCashRequestService implements CoreEnquiryService<CardLessCashGenerationResponse, CardLessCashGenReq>{
	 private final AccountClient accountClient;
	 private final ExternalErrorCodeConfig errorCodeConfig;
	 public Response<CardLessCashGenerationResponse> cardLessCashRemitGenerationRequest(CardLessCashGenReq cardLessCashGenReq) {
		 return accountClient.cardLessCashRemitGenerationRequest(cardLessCashGenReq);
	}
	@Override
	public Response<CardLessCashGenerationResponse> doCall(CardLessCashGenReq cardLessCashGenReq) {
		return accountClient.cardLessCashRemitGenerationRequest(cardLessCashGenReq);
	}
	@Override
	public Map<String, String> errorMap() {
		 return errorCodeConfig.getAccountDetailsExternalErrorCodesMap();
	}
	@Override
	public Response<CardLessCashGenerationResponse> defaultSuccessResponse() {
		return new Response<CardLessCashGenerationResponse>();
	}
	@Override
	public TransferErrorCode assignDefaultErrorCode() {
		 return ACC_EXTERNAL_SERVICE_ERROR;
	}
	@Override
	public TransferErrorCode assignFeignConnectionErrorCode() {
		 return ACC_SERVICE_CONNECTION_ERROR;
	}
}
