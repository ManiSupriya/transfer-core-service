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

    OWN_ACCOUNT("own-account","OWN_ACCOUNT","AC"),
    WITHIN_MASHREQ("within-mashreq","WITHIN_MASHREQ","WM"),
    LOCAL("local","LOCAL","LC"),
    INTERNATIONAL("international","INTERNATIONAL","IN"),
    DAR_AL_BER("dar-al-ber","DAR_AL_BER","DA"),
    BAIT_AL_KHAIR("bait-al-khair","BAIT_AL_KHAIR","BA"),
    DUBAI_CARE("dubai-care","DUBAI_CARE","DC"),
    QUICK_REMIT("quick-remit","QUICK_REMIT","QR"),
    XAU("xau","GOLD_TRANSFER","GL"),
    XAG("xag","SILVER_TRANSFER","SL")
    ;

    private String name;
    private String eventPrefix;
    private static final Map<String, String> codeTypeLookup = Stream.of(ServiceType.values())
            .collect(Collectors.toMap(ServiceType::getName,ServiceType::getCode ));

    private static final Map<String, ServiceType> serviceTypeLookup = Stream.of(ServiceType.values())
            .collect(Collectors.toMap(ServiceType::getName, serviceType -> serviceType));
    private String code;

    public static ServiceType getServiceByType(String name) {
        if (!serviceTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_PAYMENT_OPTIONS, INVALID_PAYMENT_OPTIONS.getErrorMessage());

        return serviceTypeLookup.get(name);
    }

    ServiceType(String name, String eventPrefix,String code) {
        this.name = name;
        this.eventPrefix = eventPrefix;
        this.code= code;
    }

    public static String getCodeByType(String name) {
        if (!serviceTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_PAYMENT_OPTIONS, INVALID_PAYMENT_OPTIONS.getErrorMessage());
        return codeTypeLookup.get(name);
    }

    public String getEventPrefix() {
        return eventPrefix;
    }

    public String getName() {
        return name;
    }

    public String getCode(){
        return code;
    }

}
