package com.mashreq.transfercoreservice.soap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "soap.service")
public class SoapServiceProperties {
    private String appId;
    private String originId;
    private String userId;
    private Integer readTimeout;
    private Integer connectTimeout;
}