package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.notification.client.freemarker.TemplateRequest;
import com.mashreq.notification.client.freemarker.TemplateType;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
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
    private String requestToPayMultipleNpss;

    private String requestToPayMultipleFailNpss;
    private String sendMoneyNpssSuccess;
    private String sendMoneyNpssFail;

    private String requestToPayNpss;

    private String requestToPayNpssFail;

    public String getSMSTemplate(String type, CustomerNotification customerNotification) {
        if(type.contains("CUSTOMER_ENROLL_NPSS")) {
            return MessageFormat.format(customerEnrolledForNpss, customerNotification.getCustomerName());
        } else if (NotificationType.PAYMENT_SUCCESS.equalsIgnoreCase(type)) {
            return MessageFormat.format(sendMoneyNpssSuccess, customerNotification.getAmount(),customerNotification.getBeneficiaryName());
        } else if (NotificationType.PAYMENT_FAIL.equalsIgnoreCase(type)) {
            return MessageFormat.format(sendMoneyNpssFail, customerNotification.getAmount()
                    , customerNotification.getBeneficiaryName());
        } else if (NotificationType.PAYMENT_REQUEST_SENT_MULTIPLE_RTP.equalsIgnoreCase(type)) {
            return MessageFormat.format(requestToPayMultipleNpss, customerNotification.getCustomerName());
        } else if(NotificationType.PAYMENT_REQUEST_SENT_MULTIPLE_FAIL_RTP.equalsIgnoreCase(type)){
            return MessageFormat.format(requestToPayMultipleFailNpss, customerNotification.getAmount());
        }
        else if (NotificationType.PAYMENT_REQUEST_SENT_FAIL_RTP.equalsIgnoreCase(type)) {
            return MessageFormat.format(requestToPayNpssFail, customerNotification.getAmount()
                    , customerNotification.getBeneficiaryName());
        } else if (NotificationType.PAYMENT_REQUEST_SENT_RTP.equalsIgnoreCase(type)) {
            return MessageFormat.format(requestToPayNpss, customerNotification.getAmount()
                    , customerNotification.getBeneficiaryName());
        } else {
            return "";
        }
    }
}
