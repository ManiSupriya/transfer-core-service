package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.service.EmailUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@ConfigurationProperties(prefix = "app.notification.sms")
@Data
public class SMSConfig {

    @Autowired
    private EmailUtil emailUtil;

    private String priority;
    private String serviceId;
    private String ownAccountTransactionInitiated;
    private String customerEnrolledForNpss;
    private String plSiCreation;
    private String mashreqServiceId;
    private String neoServiceId;

    public String getSMSTemplate(String type, CustomerNotification customerNotification) {
        if (type.contains("PL") && type.contains("CREATION")) {
            return MessageFormat.format(plSiCreation, customerNotification.getBeneficiaryName(), emailUtil.doMask(customerNotification.getCreditAccount()), customerNotification.getSegment().getCustomerCareNumber());
        } else if(type.contains("CUSTOMER_ENROLL_NPSS")){
            return MessageFormat.format(customerEnrolledForNpss,customerNotification.getCustomerName());
        }else
        {
            return MessageFormat.format(ownAccountTransactionInitiated, customerNotification.getCurrency(), customerNotification.getAmount(), customerNotification.getTxnRef(), customerNotification.getSegment().getCustomerCareNumber());
        }
    }
}
