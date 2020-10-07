package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.*;

@Component
@ConfigurationProperties(prefix = "app.notification.sms")
@Data
public class SMSConfig {
    private String priority;
    private String serviceId;
    private String callCenterNo;
    private String ownAccountTransactionInitiated;

    public String getSMSTemplate(String type, CustomerNotification customerNotification) {
        if(type.equals(OWN_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        if(type.equals(LOCAL_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),callCenterNo);
        }
        if(type.equals(OTHER_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),callCenterNo);
        }
        return "";
    }
}
