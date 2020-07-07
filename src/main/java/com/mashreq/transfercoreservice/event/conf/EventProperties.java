package com.mashreq.transfercoreservice.event.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "events")
public class EventProperties {
    private Map<String, String> map;
}
