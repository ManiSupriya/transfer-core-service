package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.math.BigDecimal;
import java.util.List;

import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DealConversionRateResponseDto {
	private BigDecimal exchangeRate;
	private BigDecimal accountCurrencyAmount;
	private BigDecimal transactionAmount;
	private DealEnquiryRes dealEnquiry;
	private List<MoneyTransferPurposeDto> purposeCodesList;
}
