package com.mashreq.transfercoreservice.config;

import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.OWN_ACCOUNT_TRANSACTION;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.LOCAL_ACCOUNT_TRANSACTION;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;

@Component
@ConfigurationProperties(prefix = "app.sms")
@Data
public class SMSConfig {
    private String priority;
    private String serviceId;
    private String callCenterNo;
    private String ownAccountTransactionInitiated;

    public String getSMSTemplate(String type, CustomerNotification customerNotification) {
        if(type.equals(OWN_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),callCenterNo);
        }
        if(type.equals(LOCAL_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),callCenterNo);
        }
        if(type.equals(OTHER_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),callCenterNo);
        }
        return "";
    }
}
