package com.mashreq.transfercoreservice.paymentoptions;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PAYMENT_OPTIONS;

/**
 * @author shahbazkh
 */
@Slf4j
public enum PaymentOptionType {

    TRANSFER_OPTION_OWN_ACC("own-account"),
    TRANSFER_OPTION_MASHREQ("within-mashreq"),
    TRANSFER_OPTION_LOCAL("local"),
    TRANSFER_OPTION_INTERNATIONAL("international");

    private String name;

    PaymentOptionType(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     *
     * Reverse lookup for PaymentMode
     */
    private static final Map<String, PaymentOptionType> paymentOptionLookup = Stream.of(PaymentOptionType.values())
            .collect(Collectors.toMap(PaymentOptionType::getName, paymentOptionType -> paymentOptionType));

    public static PaymentOptionType getPaymentOptionsByType(String name) {
        if (!paymentOptionLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_PAYMENT_OPTIONS, INVALID_PAYMENT_OPTIONS.getMessage());

        return paymentOptionLookup.get(name);
    }

    public String getName() {
        return name;
    }
}
