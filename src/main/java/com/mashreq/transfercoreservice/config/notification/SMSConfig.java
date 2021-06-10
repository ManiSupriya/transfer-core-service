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
    private String plSiCreation;
    private String mashreqServiceId;
    private String neoServiceId;

    public String getSMSTemplate(String type, CustomerNotification customerNotification) {
        if(type.equals(OWN_ACCOUNT_FT)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        /**
         * BUG 36630 - Local & INFT - SMS - customer contact center number is not correct in SMS for fund transfer
         * 
         *  change: 
         * this is fix for the wrong customer care details in SMS, Now SMS Info populating based on the Segment from segment_ms table
         * 
         */
        if(type.equals(LOCAL_FT)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        if(type.equals(INFT_TRANSACTION)){
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getChannel(),customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
        if(type.contains("PL") && type.contains("CREATION")){
            return MessageFormat.format(plSiCreation, customerNotification.getBeneficiaryName(), customerNotification.getCreditAccount(),customerNotification.getSegment().getCustomerCareNumber());
        }
        return "";
    }
}
