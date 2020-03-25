package com.mashreq.transfercoreservice.middleware;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "esb-service")
public class SoapServiceProperties {
    private String appId;
    private String originId;
    private String userId;
    private Integer readTimeout;
    private Integer connectTimeout;
    private ServiceCodes serviceCodes;


    @Data
    public static class ServiceCodes{
        private String ibanSearch;
        private String routingCodeSearch;
    }
}