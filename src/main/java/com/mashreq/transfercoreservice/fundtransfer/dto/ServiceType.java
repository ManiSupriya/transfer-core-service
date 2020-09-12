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

    WYMA("WYMA","OWN_ACCOUNT","AC"),
    WAMA("WAMA","WITHIN_MASHREQ","WM"),
    LOCAL("LOCAL","LOCAL","LC"),
    INFT("INFT","INTERNATIONAL","IN"),
    DAR_AL_BER("DALBR","DAR_AL_BER","DA"),
    BAIT_AL_KHAIR("BALKH","BAIT_AL_KHAIR","BA"),
    DUBAI_CARE("DCARE","DUBAI_CARE","DC"),
    QRT("QRT","QUICK_REMIT","QR"),
    XAU("XAU","GOLD_TRANSFER","GL"),
    XAG("XAG","SILVER_TRANSFER","SL"),
    CLC("cardless-cash","CARDLESS_CASH","cardless-cash")
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
