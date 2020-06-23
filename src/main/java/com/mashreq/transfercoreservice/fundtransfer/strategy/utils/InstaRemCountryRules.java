package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shahbazkh
 * @date 5/20/20
 */

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "instarem.rules")
public class InstaRemCountryRules {


    private HashMap<String, Map<String,String>> bankCodeType;

    public HashMap<String, Map<String, String>> getBankCodeType() {
        return bankCodeType;
    }

    public void setBankCodeType(HashMap<String, Map<String, String>> bankCodeType) {
        this.bankCodeType = bankCodeType;
    }
}
