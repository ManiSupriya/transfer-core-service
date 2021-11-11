package com.mashreq.transfercoreservice.fundtransfer.dto;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CHARGE_BEARER;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.annotations.ValidEnum;

public enum ChargeBearer implements ValidEnum {

    O("O"),/** remitter */
    B("B"),/**  receiver */
    U("U");/** shared */

    private String name;

    ChargeBearer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    private static final Map<String, ChargeBearer> chargeBearerLookup = Stream.of(ChargeBearer.values())
            .collect(Collectors.toMap(ChargeBearer::getName, Function.identity()));

    public static ChargeBearer getChargeBearerByName(String name) {
        if (!chargeBearerLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_CHARGE_BEARER, INVALID_CHARGE_BEARER.getErrorMessage());

        return chargeBearerLookup.get(name);
    }
}
