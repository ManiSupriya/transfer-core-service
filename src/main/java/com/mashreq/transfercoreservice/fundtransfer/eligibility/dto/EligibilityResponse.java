package com.mashreq.transfercoreservice.fundtransfer.eligibility.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EligibilityResponse<T> {
	private FundsTransferEligibility status;
	private String errorCode;
	private String errorMessage;
	private String errorDetails;
	private T data;
}
