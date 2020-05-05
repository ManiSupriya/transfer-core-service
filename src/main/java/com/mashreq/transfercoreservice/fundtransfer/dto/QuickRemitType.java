package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum QuickRemitType {

    INDIA("IN"),
    PAKISTAN("PK"),
    INSTAREM("INSTAREM");

    private String name;

    private static final Map<String, QuickRemitType> quickRemitTypeLookup = Stream.of(QuickRemitType.values())
            .collect(Collectors.toMap(QuickRemitType::getName, serviceType -> serviceType));

    public static QuickRemitType getServiceByCountry(String name) {
        if (INDIA.getName().equals(name) || PAKISTAN.getName().equals(name))
            return quickRemitTypeLookup.get(name);
        return INSTAREM;
    }

    QuickRemitType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
