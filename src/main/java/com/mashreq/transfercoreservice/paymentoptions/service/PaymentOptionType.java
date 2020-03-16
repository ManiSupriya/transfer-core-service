package com.mashreq.transfercoreservice.paymentoptions.service;

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

    TRANSFER_OPTION_OWN_ACC("own-account") {
        @Override
        String prefixCode() {
            return "fto";
        }
    },
    TRANSFER_OPTION_MASHREQ("within-mashreq") {
        @Override
        String prefixCode() {
            return "ftm";
        }
    },
    TRANSFER_OPTION_LOCAL("local") {
        @Override
        String prefixCode() {
            return "ftl";
        }
    },
    TRANSFER_OPTION_INTERNATIONAL("international") {
        @Override
        String prefixCode() {
            return "fti";
        }
    };

    abstract String prefixCode();

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
            GenericExceptionHandler.handleError(INVALID_PAYMENT_OPTIONS, INVALID_PAYMENT_OPTIONS.getErrorMessage());

        return paymentOptionLookup.get(name);
    }

    public String getName() {
        return name;
    }
}
