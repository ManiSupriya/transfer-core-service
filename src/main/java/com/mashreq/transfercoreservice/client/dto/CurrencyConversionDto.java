package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrencyConversionDto {
    private BigDecimal accountCurrencyAmount;
    private BigDecimal transactionAmount;

    //Only send exchange rates
    private BigDecimal exchangeRate;
}
