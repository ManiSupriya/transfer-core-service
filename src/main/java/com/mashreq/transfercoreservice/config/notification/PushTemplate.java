package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.transfer.m2m.model.CustomerNotification;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.mashreq.transfer.m2m.constants.NotificationType.*;

@Data
@Component
@ConfigurationProperties(prefix = "app.notification.push")
public class PushTemplate {

    private String successTransferPushTmpl;
    private String failedTransferPushTmpl;
    private String registerAccountPushTmpl;
    private String validity;

    public String getPushTemplate(String type, CustomerNotification customerNotification) {
        if (type.equalsIgnoreCase(TRANSFER_SUCCESS_SEND)) {
            return MessageFormat.format(successTransferPushTmpl, customerNotification.getSendCurrency(), customerNotification.getAmount(), customerNotification.getBenMobile());
        } else if (type.equalsIgnoreCase(REGISTER_SEND)) {
            return MessageFormat.format(registerAccountPushTmpl, customerNotification.getSendCurrency(), customerNotification.getAmount(), customerNotification.getBenMobile(), validity);
        } else if (type.equals(TRANSFER_FAILED_SEND)) {
            return MessageFormat.format(failedTransferPushTmpl, customerNotification.getSendCurrency(), customerNotification.getAmount(), customerNotification.getBenMobile());
        }
        return "";
    }

}
