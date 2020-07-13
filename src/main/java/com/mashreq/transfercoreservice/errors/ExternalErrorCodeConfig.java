package com.mashreq.transfercoreservice.errors;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "external.error-code.mapper")
public class ExternalErrorCodeConfig {
	private Accounts accounts;
	private OTP otp;
	
	public Map<String, String> getAccountDetailsExternalErrorCodesMap() {
        return accounts.getDetailCallErrorMap();
    }
	
	public Map<String, String> getOTPDetailsExternalErrorCodesMap() {
        return otp.getDetailCallErrorMap();
    }
	
	@Data
    protected static class OTP {
        private Map<String, String> detailCallErrorMap;
    }
	
	@Data
    protected static class Accounts {
        private Map<String, String> detailCallErrorMap;
    }
}
