package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

@Data
public class CustomerNotification {

    String currency;
    String amount;
    String txnRef;
    String exchangeRate;
    String transferType;
    String fromAccount;
    String fromCurrency;
    String fromAmount;
    String toAccount;
    String toAmount;
    String toCurrency;

}
