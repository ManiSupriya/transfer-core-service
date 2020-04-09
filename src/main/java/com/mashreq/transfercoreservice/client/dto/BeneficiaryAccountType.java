package com.mashreq.transfercoreservice.client.dto;

import com.mashreq.transfercoreservice.annotations.ValidEnum;

public enum BeneficiaryAccountType implements ValidEnum {
	INDIVIDUAL,
	COMPANY,
	EXCHANGE;

	private String name;

	public String getName() {
		return name;
	}
}
