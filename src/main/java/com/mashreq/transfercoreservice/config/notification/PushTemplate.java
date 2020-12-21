package com.mashreq.transfercoreservice.config.notification;
/**
 * BUG 36630 - Local & INFT - SMS - customer contact center number is not correct in SMS for fund transfer
 * 
 *  update: 
 * this is fix for the wrong customer care details in SMS, Now SMS Info populating based on the Segment from segment_ms table
 * 
 */

import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.mashreq.transfercoreservice.notification.model.NotificationType.LOCAL_ACCOUNT_TRANSACTION;
import static com.mashreq.transfercoreservice.notification.model.NotificationType.OTHER_ACCOUNT_TRANSACTION;
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
        if(type.equals(LOCAL_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        if(type.equals(OTHER_ACCOUNT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        return "";
    }

}
