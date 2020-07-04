package com.mashreq.transfercoreservice.event.model;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum EventType {

    WITHIN_MASHREQ_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "WITHIN MASHREQ transfer request has been received"),
    LOCAL_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "LOCAL FUND transfer request has been received"),
    OWN_ACCOUNT_TRANSFER_REQUEST("FUND_TRANSFER", "OWN ACCOUNT transfer request has been received"),
    INTERNATIONAL_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "INTERNATIONAL Fund transfer request has been received"),
    DAR_AL_BER_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "DAR AL BER Fund transfer request has been received"),
    BAIT_AL_KHAIR_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "BAIT AL KHAIR Fund transfer request has been received"),
    DUBAI_CARE_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "DUBAI CARE Fund transfer request has been received"),
    QUICK_REMIT_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "QUICK REMIT Fund transfer request has been received"),
    CHARITY_ACCOUNT_VALIDATION("FUND_TRANSFER", "Charity Account Validation"),


    ACCOUNT_BELONGS_TO_CIF("FUND_TRANSFER", "Account belongs to cif"),
    BALANCE_VALIDATION("FUND_TRANSFER", "Balance Validation"),
    BENEFICIARY_VALIDATION("FUND_TRANSFER", "Beneficiary validation"),


    OWN_ACCOUNT_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Own account fund transfer request end status"),
    WITHIN_MASHREQ_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Within Mashreq fund transfer request end status"),
    LOCAL_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Local fund transfer request end status"),
    INTERNATIONAL_FUND_TRANSFER_ENDS("FUND_TRANSFER", "International fund transfer request end status"),
    DAR_AL_BER_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Dar Al Ber Charity fund transfer request end status"),
    BAIT_AL_KHAIR_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Bait Al Khair Charity fund transfer request end status"),
    DUBAI_CARE_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Dubai Care Charity fund transfer request end status"),
    QUICK_REMIT_FUND_TRANSFER_ENDS("FUND_TRANSFER", "Quick remit fund transfer request end status"),
    OWN_ACCOUNT_MW_CALL("FUND_TRANSFER", ""),

    CURRENCY_VALIDATION("FUND_TRANSFER", "Currency Validation"),
    FIN_TRANSACTION_VALIDATION("FUND_TRANSFER", "Financial Transaction Number Validation"),
    IBAN_VALIDATION("FUND_TRANSFER", "Iban Validation"),
    PAYMENT_PURPOSE_VALIDATION("FUND_TRANSFER", "Payment Purpose Validation"),
    SAME_ACCOUNT_VALIDATION("FUND_TRANSFER", "Check account credit and deit account should not be same");


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
