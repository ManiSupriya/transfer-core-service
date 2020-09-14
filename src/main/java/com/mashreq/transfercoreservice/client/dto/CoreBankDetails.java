package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreBankDetails {
	@JsonProperty("BIC")
	private String swiftCode;
	@JsonProperty("BankName")
	private String bankName;
	@JsonProperty("CTRYName")
	private String bankCountry;
	@JsonProperty("CityName")
	private String bankCity;
}
