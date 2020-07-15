package com.mashreq.transfercoreservice.client.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.OTP_EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.OTP_SERVICE_CONNECTION_ERROR;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.logcore.annotations.TrackExec;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.VerifyOTPClient;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@TrackExec
@Slf4j
@RequiredArgsConstructor
public class VerifyOTPService implements CoreEnquiryService<VerifyOTPResponseDTO, VerifyOTPRequestDTO>{
	private final VerifyOTPClient otpClient;
	 private final ExternalErrorCodeConfig errorCodeConfig;
	 public Response<VerifyOTPResponseDTO> blockCardLessCashRequest(VerifyOTPRequestDTO verifyOtpReq) {
		 return otpClient.verifyOTP(verifyOtpReq);
	}
	@Override
	public Response<VerifyOTPResponseDTO> doCall(VerifyOTPRequestDTO verifyOtpReq) {
		return otpClient.verifyOTP(verifyOtpReq);
	}
	@Override
	public Map<String, String> errorMap() {
		 return errorCodeConfig.getOTPDetailsExternalErrorCodesMap();
	}
	@Override
	public Response<VerifyOTPResponseDTO> defaultSuccessResponse() {
		return new Response<VerifyOTPResponseDTO>();
	}
	@Override
	public TransferErrorCode assignDefaultErrorCode() {
		 return OTP_EXTERNAL_SERVICE_ERROR;
	}
	@Override
	public TransferErrorCode assignFeignConnectionErrorCode() {
		 return OTP_SERVICE_CONNECTION_ERROR;
	}
	@Override
	public void assignCustomErrorCode(String errorDetail, TransferErrorCode errorCode) {
		GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage(), appendToErrorDetails(errorCode.getErrorMessage(), errorDetail));
	}
	
	private String appendToErrorDetails(String responseErrorMessage, String responseErrorCode) {
        return StringUtils.isBlank(responseErrorMessage)
                ? responseErrorCode
                : responseErrorMessage + "," + responseErrorCode;
    }
}
