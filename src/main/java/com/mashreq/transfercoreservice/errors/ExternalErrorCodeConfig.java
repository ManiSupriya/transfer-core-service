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
    private Middleware middleware;
	private OTP otp;
	private Deal deal;
	
	public Map<String, String> getDealDetailsExternalErrorCodesMap() {
        return deal.getDetailCallErrorMap();
    }
	
	public Map<String, String> getAccountDetailsExternalErrorCodesMap() {
        return accounts.getDetailCallErrorMap();
    }

    public Map<String, String> getMiddlewareExternalErrorCodesMap() {
        return middleware.getDetailCallErrorMap();
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

    @Data
    protected static class Middleware {
        private Map<String, String> detailCallErrorMap;
    }
	
	@Data
    protected static class Deal {
        private Map<String, String> detailCallErrorMap;
    }
}
