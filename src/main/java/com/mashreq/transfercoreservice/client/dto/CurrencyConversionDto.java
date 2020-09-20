package com.mashreq.transfercoreservice.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class CurrencyConversionDto {
    private BigDecimal accountCurrencyAmount;
    private BigDecimal transactionAmount;

    //Only send exchange rates
    private BigDecimal exchangeRate;
}
