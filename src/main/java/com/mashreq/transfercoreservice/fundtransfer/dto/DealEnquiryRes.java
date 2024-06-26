package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DealEnquiryRes {
	private String dealNo;
	private String dealDate;
	private String internalDealRefNo;
	private String buyCurrency;
	private String sellCurrency;
	private String dealCurrency;
	private String dealStatus;
	private String totalUtilizedAmount;
	private BigDecimal dealRate;
	private BigDecimal dealAmount;
	private String dealExpiryDate;
    private String dealAuthStatus;
    private String dealTxnStatus;
}
