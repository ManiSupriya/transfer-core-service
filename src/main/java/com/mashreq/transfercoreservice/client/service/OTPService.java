package com.mashreq.transfercoreservice.client.service;
import org.springframework.stereotype.Service;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class OTPService {
	private final VerifyOTPService verifyOTPService;
	public Response<VerifyOTPResponseDTO> verifyOTP(VerifyOTPRequestDTO verifyOtpReq) {
		log.info("verify OTP Request {} ", verifyOtpReq);
		return verifyOTPService.getResponse(verifyOtpReq);
	}
}