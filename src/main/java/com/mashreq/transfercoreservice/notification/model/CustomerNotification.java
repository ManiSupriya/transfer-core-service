package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

@Data
public class CustomerNotification {

    String currency;
    String amount;
    String txnRef;
}
