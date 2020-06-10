package com.mashreq.transfercoreservice.config.http;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.mashreq.transfercoreservice.common.HeaderNames.CHANNEL_HEADER_MOBILE;
import static com.mashreq.transfercoreservice.common.HeaderNames.CHANNEL_HEADER_WEB;

/**
 * @author shahbazkh
 * @date 2/27/20
 */

@Component
public class ChannelTracerGenerator {

    private static final String traceTemplate = "{CHANNEL}{REGION}{CIF}{DATE-TIME-FORMAT}";

    public String channelTraceId(final String channelName, final String cifId, final String country) {

        //TODO Month Map to be confirmed with Bala
        String monthValue = monthMap.get(LocalDateTime.now().getMonthValue());
        String dateTime = DateTimeFormatter.ofPattern("ddHHmms").format(LocalDateTime.now());
        return traceTemplate
                .replace("{CHANNEL}", "")
                .replace("{REGION}", "")
                .replace("{CIF}", cifId.substring(2))
                .replace("{DATE-TIME-FORMAT}", monthValue + dateTime);
    }

    //TODO To be confirmed with Bala
    private static final Map<Integer, String> monthMap = new HashMap<Integer, String>() {{
        put(1, "A");
        put(2, "B");
        put(3, "C");
        put(4, "D");
        put(5, "E");
        put(6, "F");
        put(7, "G");
        put(8, "H");
        put(9, "I");
        put(10, "J");
        put(11, "K");
        put(12, "L");
    }};

    private String getChannel(String channelName) {
        if (CHANNEL_HEADER_MOBILE.equalsIgnoreCase(channelName)) {
            return "M";
        } else if (CHANNEL_HEADER_WEB.equalsIgnoreCase(channelName)) {
            return "W";
        } else {
            return "U";
        }
    }

}
