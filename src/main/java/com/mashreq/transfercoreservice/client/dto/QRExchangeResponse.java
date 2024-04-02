package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRExchangeResponse {

    private String exchangeRate;

    private String accountCurrencyAmount;
    private String accountCurrency;

    private String transactionAmount;
    private String transactionCurrency;

    private String chargeCurrency;
    private String chargeAmount;

    private String convertedAmount;
    private String debitAmountWithoutCharges;

    private boolean allowQR;
    private String gatewayType;
    private String exchangeRateDisplay;

    private String maxAmountDaily;
    private String errorCode;
    private String errorMessage;
}