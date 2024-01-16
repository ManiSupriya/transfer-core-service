package com.mashreq.transfercoreservice.config.feign;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@ConfigurationProperties(prefix = "app.services.omw-external")
@Configuration
@Data
public class OmwExternalConfigProperties {

    private String scope;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String url;
    private String tokenUrl;
    private String serviceId;
    private boolean titleFetchEnabled;
}
