package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
/**
 * 
 * @author SURESH
 *
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class VerifyOTPResponseDTO {
	
	boolean authenticated; 
	String sessionToken;

}