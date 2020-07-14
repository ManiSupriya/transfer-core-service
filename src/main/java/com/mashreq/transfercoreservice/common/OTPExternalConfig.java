package com.mashreq.transfercoreservice.common;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;
/**
 * 
 * @author SURESH
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "external.otp-code.mapper")
public class OTPExternalConfig {
	private OtpVerify otp;
	public int getDpPublicKey() {
		return otp.getDpPublicKey();
	}
	public String getDpRandomNumber() {
		return otp.getDpRandomNumber();
	}
	@Data
	protected static class OtpVerify {
		private int dpPublicKey;
		private String dpRandomNumber;
	}
}