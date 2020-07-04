package com.mashreq.transfercoreservice.event.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "events")
public class EventProperties {
    private Map<String, String> map;
}
