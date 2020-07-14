package com.mashreq.transfercoreservice.client.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * 
 * @author SURESH
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class VerifyOTPRequestDTO {
	private String loginId;
	private String challengeToken;
	private String otp;
	private String dpRandomNumber;
	private Integer dpPublicKeyIndex;
	private String redisKey;
}
