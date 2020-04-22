package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.annotations.ValidEnum;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PAYMENT_OPTIONS;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public enum ServiceType implements ValidEnum {

    OWN_ACCOUNT("own-account"),
    WITHIN_MASHREQ("within-mashreq"),
    LOCAL("local"),
    INTERNATIONAL("international"),
    DAR_AL_BER("dar-al-ber"),
    BAIT_AL_KHAIR("bait-al-khair"),
    DUBAI_CARE("dubai-care"),
    QUICK_REMIT("quick-remit");

    private String name;


    private static final Map<String, ServiceType> serviceTypeLookup = Stream.of(ServiceType.values())
            .collect(Collectors.toMap(ServiceType::getName, serviceType -> serviceType));

    public static ServiceType getServiceByType(String name) {
        if (!serviceTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_PAYMENT_OPTIONS, INVALID_PAYMENT_OPTIONS.getErrorMessage());

        return serviceTypeLookup.get(name);
    }

    ServiceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
