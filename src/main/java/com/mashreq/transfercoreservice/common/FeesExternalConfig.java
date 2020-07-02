package com.mashreq.transfercoreservice.common;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "external.fees-code.mapper")
public class FeesExternalConfig {
private Fees fees;
	
	
	public String getCardLessCashExternalFee() {
        return fees.getCardLessCashFees();
    }
	
	@Data
    protected static class Fees {
        private String cardLessCashFees;
    }
}
