package com.mashreq.transfercoreservice.notification.model;

import com.mashreq.transfercoreservice.model.Segment;
import lombok.Data;

@Data
public class CustomerNotification {

    String currency;
    String amount;
    String txnRef;
    String exchangeRate;
    String transferType;
    String debitAccount;
    String debitCurrency;
    String debitAmount;
    String creditAccount;
    String creditAmount;
    String creditCurrency;
    String buy_sell;
    Segment segment;
    String channel;

}
