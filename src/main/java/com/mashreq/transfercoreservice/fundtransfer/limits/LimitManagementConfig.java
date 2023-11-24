package com.mashreq.transfercoreservice.fundtransfer.limits;

import com.mashreq.transfercoreservice.notification.model.EmailParameters;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "limit.eligible")
@Getter
@Setter
public class LimitManagementConfig {

    private HashMap<String, List<String>> countries;
}
