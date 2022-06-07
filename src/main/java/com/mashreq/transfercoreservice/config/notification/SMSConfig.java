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
    private String ownAccountTransactionInitiated;
    private String plSiCreation;
    private String mashreqServiceId;
    private String neoServiceId;

    public String getSMSTemplate(String type, CustomerNotification customerNotification) {
        if(type.contains("PL") && type.contains("CREATION")){
            return MessageFormat.format(plSiCreation, customerNotification.getBeneficiaryName(), customerNotification.getCreditAccount(),customerNotification.getSegment().getCustomerCareNumber());
        }
        else {
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getCurrency(),customerNotification.getAmount(),customerNotification.getTxnRef(),customerNotification.getSegment().getCustomerCareNumber());
        }
    }
}
