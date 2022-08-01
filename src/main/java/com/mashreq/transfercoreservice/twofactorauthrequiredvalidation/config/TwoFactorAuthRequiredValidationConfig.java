package com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "two-factor-authentication")
public class TwoFactorAuthRequiredValidationConfig {
	private Integer durationInHours;
	private Boolean twofactorAuthRelaxed;
	private Integer maxAmountAllowed;
	private Integer noOfTransactionsAllowed;

}
