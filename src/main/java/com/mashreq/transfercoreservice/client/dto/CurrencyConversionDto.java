package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CurrencyConversionDto {
    private BigDecimal accountCurrencyAmount;
    private BigDecimal transactionAmount;

    //Only send exchange rates
    private BigDecimal exchangeRate;
}
