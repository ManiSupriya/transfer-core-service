package com.mashreq.transfercoreservice.config.notification;

import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Configuration class for request parameters for sending email
 *
 * @author Thanigachalam P
 */
@Component
@ConfigurationProperties(prefix = "app.notification")
@Getter @Setter
public class EmailConfig {

    private HashMap<String, EmailParameters> email;
    private String alternateSteps;

}