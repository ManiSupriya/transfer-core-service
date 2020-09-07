package com.mashreq.transfercoreservice.middleware;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "esb-service.service-codes")
public class EsbServiceCodes {
	private String fundTransfer;
    private String ibanSearch;
    private String routingCodeSearch;
    private String ifscSearch;
    private String flexRuleEngine;
    private String quickRemitIndia;
    private String quickRemitPakistan;
    private String quickRemitInstaRem;
    private String searchAccountDetails;
    private String gpiTransactionDetails;
    private String swiftGpiTransactionDetails;
}
