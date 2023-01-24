package com.mashreq.transfercoreservice.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeneficiaryModificationValidationRequest {
	private String beneficiaryId;
	private Integer duration;
	private String durationType;
}
