package com.mashreq.transfercoreservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "escrow-accounts")
public class EscrowConfig {

    private boolean enabled;
    private List<String> trustAccounts;
    private List<String> oaAccounts;
    private String defaultProjectName;
}
