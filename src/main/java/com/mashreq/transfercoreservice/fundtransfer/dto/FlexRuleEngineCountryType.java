package com.mashreq.transfercoreservice.fundtransfer.dto;

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
public enum FlexRuleEngineCountryType implements ValidEnum {
    IN("IN"),
    PK("PK"),
    OTHERS("OTHERS");

    FlexRuleEngineCountryType(final String name) {
        this.name = name;
    }

    private String name;


    private static final Map<String, FlexRuleEngineCountryType> flexRuleEngineReverseLookup = Stream.of(FlexRuleEngineCountryType.values())
            .collect(Collectors.toMap(FlexRuleEngineCountryType::getName, routingCodeType -> routingCodeType));

    public static FlexRuleEngineCountryType getRoutingCodeByType(String name) {
        if (!flexRuleEngineReverseLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_ROUTING_CODE, INVALID_ROUTING_CODE.getErrorMessage());

        return flexRuleEngineReverseLookup.get(name);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
