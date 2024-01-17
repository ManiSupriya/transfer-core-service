package com.mashreq.transfercoreservice.promo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

import com.mashreq.mobcommons.annotations.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeTransactionRequestDto {

	private BigDecimal txnAmount;
	private String txnCurrency;
	private String serviceType;
	private String countryOfResidence;
	private String promoCode;
	private String orderStatus;
	private String fromAccount;
	
}
