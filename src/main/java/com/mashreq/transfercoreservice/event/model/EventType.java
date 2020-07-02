package com.mashreq.transfercoreservice.event.model;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum EventType {

    FUND_TRANSFER_INITIATION("FUND_TRANSFER","Fund transfer request has been received"),
    OWN_ACCOUNT_FUND_TRANSFER_ENDS("FUND_TRANSFER","Own account fund transfer request end status"),
    WITHIN_MASHREQ_FUND_TRANSFER_ENDS("FUND_TRANSFER","Within Mashreq fund transfer request end status"),
    LOCAL_FUND_TRANSFER_ENDS("FUND_TRANSFER","Local fund transfer request end status"),
    INTERNATIONAL_FUND_TRANSFER_ENDS("FUND_TRANSFER","International fund transfer request end status"),
    DAR_AL_BER_FUND_TRANSFER_ENDS("FUND_TRANSFER","Dar Al Ber Charity fund transfer request end status"),
    BAIT_AL_KHAIR_FUND_TRANSFER_ENDS("FUND_TRANSFER","Bait Al Khair Charity fund transfer request end status"),
    DUBAI_CARE_FUND_TRANSFER_ENDS("FUND_TRANSFER","Dubai Care Charity fund transfer request end status"),
    QUICK_REMIT_FUND_TRANSFER_ENDS("FUND_TRANSFER","Quick remit fund transfer request end status"),


    OWN_ACCOUNT_MW_CALL("FUND_TRANSFER",""),

    SALIK_BILL_PAYMENT_INITIATION("BILL_PAYMENT", "Bill payment initiated by user"),
    SALIK_LIMIT_VALIDATION("BILL_PAYMENT","Check spend limit of customer"),
    SALIK_BALANCE_VALIDATION("BILL_PAYMENT", "Check available balance of customer"),
    SALIK_MW_CALL("BILL_PAYMENT","Salik bill payment"),
    SALIK_BILL_PAYMENT_REQUEST("BILL_PAYMENT","Bill Payment request end state");


    private static final Map<String, EventType> eventTypeLookup = Stream.of(EventType.values())
            .collect(Collectors.toMap(EventType::name, eventType -> eventType));

    public static EventType getEventTypeByCode(String code) {
        if (!eventTypeLookup.containsKey(code))
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_EVENT_TYPE_CODE, String.format("No Event type found for requested code [ %s ]", code));
        return eventTypeLookup.get(code);
    }

    private String type;
    private String description;

    EventType(String type, String description) {
        this.type = type;
        this.description = description;
    }


}
