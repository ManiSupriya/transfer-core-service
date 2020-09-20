package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.transfercoreservice.notification.model.NotificationType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;


/**
 * email properties per segment
 *
 * @author Kalim
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.notification.email")
public class EmailTemplate {

    private String emailSubject;
    private String fromEmailName;
    private String fromEmailAddress;
    private String successTransferTmpl;

    public String getEmailTemplate(String type) {
        if (type.equalsIgnoreCase(NotificationType.TRANSFER_SUCCESS_SEND)) {
            return successTransferTmpl;
        }
        return "";
    }

    public String getEmailSubjectTmpl(String channel, String type) {
        if (type.equalsIgnoreCase(NotificationType.TRANSFER_SUCCESS_SEND)) {
            return MessageFormat.format(emailSubject, channel);
        }
        return "";
    }
}