package com.mashreq.transfercoreservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ftcc")
@Getter @Setter
public class FTCCConfig {

    private String authStatus;
    private String acwthInst5;
    private String amountTag;
    private String messageThrough;
    private String transTypeCode;
    private String merchantId;
    private String terminalId;
    private String srvCode;
}
