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
	
	
	public Map<String, String> getAccountDetailsExternalErrorCodesMap() {
        return accounts.getDetailCallErrorMap();
    }
	
	@Data
    protected static class Accounts {
        private Map<String, String> detailCallErrorMap;
    }
}
