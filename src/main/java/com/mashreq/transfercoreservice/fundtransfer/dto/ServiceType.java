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

    OWN_ACCOUNT("own-account","OWN_ACCOUNT"),
    WITHIN_MASHREQ("within-mashreq","WITHIN_MASHREQ"),
    LOCAL("local","LOCAL"),
    INTERNATIONAL("international","INTERNATIONAL"),
    DAR_AL_BER("dar-al-ber","DAR_AL_BER"),
    BAIT_AL_KHAIR("bait-al-khair","BAIT_AL_KHAIR"),
    DUBAI_CARE("dubai-care","DUBAI_CARE"),
    QUICK_REMIT("quick-remit","QUICK_REMIT");

    private String name;
    private String eventPrefix;

    private static final Map<String, ServiceType> serviceTypeLookup = Stream.of(ServiceType.values())
            .collect(Collectors.toMap(ServiceType::getName, serviceType -> serviceType));

    public static ServiceType getServiceByType(String name) {
        if (!serviceTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_PAYMENT_OPTIONS, INVALID_PAYMENT_OPTIONS.getErrorMessage());

        return serviceTypeLookup.get(name);
    }

    ServiceType(String name, String eventPrefix) {
        this.name = name;
        this.eventPrefix = eventPrefix;
    }

    public String getEventPrefix() {
        return eventPrefix;
    }

    public String getName() {
        return name;
    }

}
