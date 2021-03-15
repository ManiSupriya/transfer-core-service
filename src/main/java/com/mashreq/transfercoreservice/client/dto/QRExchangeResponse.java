package com.mashreq.transfercoreservice.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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

}