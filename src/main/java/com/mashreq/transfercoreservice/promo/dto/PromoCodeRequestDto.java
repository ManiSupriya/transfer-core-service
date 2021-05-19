package com.mashreq.transfercoreservice.promo.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequestDto {

	private BigDecimal txnAmount;
	private String txnCurrency;
	private String serviceType;
	private String countryOfResidence;
	private String promoCode;
	
}
