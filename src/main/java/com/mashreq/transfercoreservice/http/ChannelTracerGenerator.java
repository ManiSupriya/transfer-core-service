package com.mashreq.transfercoreservice.http;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author shahbazkh
 * @date 2/27/20
 */

@Component
public class ChannelTracerGenerator {

    private final String traceTemplate = "{CHANNEL}{REGION}{CIF}{DATE-TIME-FORMAT}";

    //TODO Remove hardcoded region
    public String channelTraceId(final String userAgent, final String cifId) {
        String dateTime = DateTimeFormatter.ofPattern("yyMMddHHmmssS").format(LocalDateTime.now());
        return traceTemplate.replace("{CHANNEL}", getChannel(userAgent))
                .replace("{REGION}", "AE")
                .replace("{CIF}", cifId)
                .replace("{DATE-TIME-FORMAT}", dateTime);
    }

    private String getChannel(String userAgent) {
        if ("MOBILE".equalsIgnoreCase(userAgent)) {
            return "M";
        } else if ("WEB".equalsIgnoreCase(userAgent)) {
            return "W";
        } else {
            return "U";
        }
    }
}
