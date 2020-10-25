package com.mashreq.transfercoreservice.config.notification;


import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OWN_ACCOUNT_TRANSACTION;


@Data
@Component
@ConfigurationProperties(prefix = "app.notification.push")
public class PushTemplate {

    private String ownAccountTransactionInitiated;

    public String getPushTemplate(String type, CustomerNotification customerNotification) {
        if(type.equals(OWN_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        return "";
    }

}
