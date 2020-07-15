package com.mashreq.transfercoreservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
/**
 * 
 * @author SURESH
 *
 */
@FeignClient(name = "otp", url = "${app.services.otp}", configuration = FeignConfig.class)
public interface VerifyOTPClient {
	@PostMapping(value = "/api/v1/otp/verify")
    public Response<VerifyOTPResponseDTO> verifyOTP(VerifyOTPRequestDTO verifyOtpReq);
    
}
