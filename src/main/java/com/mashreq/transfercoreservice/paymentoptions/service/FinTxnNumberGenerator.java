package com.mashreq.transfercoreservice.paymentoptions.service;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.mashreq.transfercoreservice.paymentoptions.service.PaymentOptionType.TRANSFER_OPTION_INTERNATIONAL;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
@Slf4j
public class FinTxnNumberGenerator {

    private static final String TEMPLATE = "{DOM}-{UUID}";

    public static String generate(PaymentOptionType type) {

        final String finTxnNumber = TEMPLATE.replace("{DOM}", type.prefixCode())
                .replace("{UUID}", UUID.randomUUID().toString());
        log.info("Generating Financial Transaction Number {} ", finTxnNumber);
        return finTxnNumber;
    }
}
