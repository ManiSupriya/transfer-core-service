package com.mashreq.transfercoreservice.swifttracker.dto;
import lombok.Builder;
/**
 * @author SURESH PASUPULETI
 */
import lombok.Data;

@Data
@Builder
public class ForeignExchangeDetails {
	private String FromCurrency;
	private String ToCurrency;
	private String exchangeRate;

}
