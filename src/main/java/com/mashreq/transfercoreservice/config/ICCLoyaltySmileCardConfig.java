package com.mashreq.transfercoreservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.loyalty")
@Data
public class ICCLoyaltySmileCardConfig {
	private int timeInterval;
	private String smilesProductBin;
}
