package com.mashreq.transfercoreservice.paymentoptions.service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@Slf4j
public class FinTxnNumberGenerator {

    private static final String traceTemplate = "{DOM}-{CHANNEL}{REGION}-{CIF}-{DATE-TIME-FORMAT}";

    public static String generate(final String channel, final String cifId, final PaymentOptionType type) {
        String dateTime = DateTimeFormatter.ofPattern("ddHHmms").format(LocalDateTime.now());
        return traceTemplate
                .replace("{DOM}", type.prefixCode())
                .replace("{CHANNEL}", getChannelCode(channel))
                .replace("{REGION}", "AE")
                .replace("{CIF}", cifId)
                .replace("{DATE-TIME-FORMAT}", dateTime);
    }

    private FinTxnNumberGenerator() {
    }

    private static String getChannelCode(String userAgent) {
        if ("MOBILE".equalsIgnoreCase(userAgent)) {
            return "M";
        } else if ("WEB".equalsIgnoreCase(userAgent)) {
            return "W";
        } else {
            return "U";
        }
    }

}
