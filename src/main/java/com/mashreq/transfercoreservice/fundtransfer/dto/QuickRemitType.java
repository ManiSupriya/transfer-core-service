package com.mashreq.transfercoreservice.fundtransfer.dto;


import com.mashreq.ms.exceptions.GenericExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SERVICE_TYPE;


public enum QuickRemitType {

    INDIA("IN", "QRIN"),
    PAKISTAN("PK", "QRPK"),
    INSTAREM("ANY", "QROC");

    private String countryCode;
    private String limitcode;

    QuickRemitType(String countryCode, String limitcode) {
        this.countryCode = countryCode;
        this.limitcode = limitcode;
    }

    private static final Map<String, QuickRemitType> quickRemitTypeLookup = Stream.of(QuickRemitType.values())
            .collect(Collectors.toMap(QuickRemitType::getCountryCode, serviceType -> serviceType));

    private static final Map<String, String> quickRemitCodeLookup = Stream.of(QuickRemitType.values())
            .collect(Collectors.toMap(QuickRemitType::getCountryCode, QuickRemitType::getLimitCode));


    public static String getCodeByName(String name) {
        if (!quickRemitCodeLookup.containsKey(name))
            return quickRemitCodeLookup.get(INSTAREM.getCountryCode());
        return quickRemitCodeLookup.get(name);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLimitCode() {
        return limitcode;
    }
}
