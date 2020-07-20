package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACC_SERVICE_CONNECTION_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@TrackExec
@Slf4j
@RequiredArgsConstructor
public class AccountCardLessCashQueryService implements CoreEnquiryService<List<CardLessCashQueryResponse>, CardLessCashQueryRequest>{
	 private final AccountClient accountClient;
	 private final ExternalErrorCodeConfig errorCodeConfig;
	 public Response<List<CardLessCashQueryResponse>> cardLessCashRemitQuery( String accountNumber,
			 Integer remitNumDays) {
			return accountClient.cardLessCashRemitQuery(accountNumber, remitNumDays);
	}
	@Override
	public Response<List<CardLessCashQueryResponse>> doCall(CardLessCashQueryRequest cardLessCashQueryRequest) {
		return accountClient.cardLessCashRemitQuery(cardLessCashQueryRequest.getAccountNumber(), cardLessCashQueryRequest.getRemitNumDays());
	}
	@Override
	public Map<String, String> errorMap() {
		 return errorCodeConfig.getAccountDetailsExternalErrorCodesMap();
	}
	@Override
	public Response<List<CardLessCashQueryResponse>> defaultSuccessResponse() {
		return Response.<List<CardLessCashQueryResponse>>builder().status(ResponseStatus.SUCCESS).data(new ArrayList<CardLessCashQueryResponse>()).build();
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
