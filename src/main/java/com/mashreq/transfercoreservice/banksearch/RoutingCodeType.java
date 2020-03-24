package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.annotations.ValidEnum;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_ROUTING_CODE;

/**
 * @author shahbazkh
 * @date 3/23/20
 */
public enum RoutingCodeType implements ValidEnum {
    US("ABA"),
    GB("UKSORT"),
    NZ("NZBA"),
    IN("IFSC"),
    AU("BSB"),
    CA("TRNO");

    RoutingCodeType(final String name) {
        this.name = name;
    }

    private String name;


    private static final Map<String, RoutingCodeType> routingCodeTypeLookup = Stream.of(RoutingCodeType.values())
            .collect(Collectors.toMap(RoutingCodeType::getName, routingCodeType -> routingCodeType));

    public static RoutingCodeType getRoutingCodeByType(String name) {
        if (!routingCodeTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_ROUTING_CODE, INVALID_ROUTING_CODE.getErrorMessage());

        return routingCodeTypeLookup.get(name);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
