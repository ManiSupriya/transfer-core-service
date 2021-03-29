package com.mashreq.transfercoreservice.fundtransfer.eligibility.dto;

import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EligibilityResponse<T> {
	private FundsTransferEligibility status;
	private String errorCode;
	private String errorMessage;
	private T data;
}
