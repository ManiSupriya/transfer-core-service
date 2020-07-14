package com.mashreq.transfercoreservice.event;

import com.mashreq.mobcommons.services.events.model.EventType;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum FundTransferEventType implements EventType {

    WITHIN_MASHREQ_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "WITHIN MASHREQ transfer request has been received"),
    LOCAL_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "LOCAL FUND transfer request has been received"),
    OWN_ACCOUNT_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "OWN ACCOUNT transfer request has been received"),
    INTERNATIONAL_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "INTERNATIONAL Fund transfer request has been received"),
    DAR_AL_BER_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "DAR AL BER Fund transfer request has been received"),
    BAIT_AL_KHAIR_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "BAIT AL KHAIR Fund transfer request has been received"),
    DUBAI_CARE_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "DUBAI CARE Fund transfer request has been received"),
    QUICK_REMIT_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "QUICK REMIT Fund transfer request has been received"),
    CARD_LESS_CASH_GENERATION_REQUEST("CARDLESS-CASH", "CARDLESS-CASH request generation success"),
    CARD_LESS_CASH_QUERY_DETAILS("CARDLESS-CASH", "CARDLESS-CASH query details received"),
    CARD_LESS_CASH_BLOCK_REQUEST("CARDLESS-CASH", "CARDLESS-CASH block request success"),
    CARD_LESS_CASH_GENERATION_FAILED("CARDLESS-CASH", "CARDLESS-CASH request generation failed"),
    CARD_LESS_CASH_QUERY_DETAILS_FAILED("CARDLESS-CASH", "CARDLESS-CASH query details failed"),
    CARD_LESS_CASH_BLOCK_REQUEST_FAILED("CARDLESS-CASH", "CARDLESS-CASH block request failed"),

    CHARITY_ACCOUNT_VALIDATION("FUND_TRANSFER", "Charity Account Validation"),
    ACCOUNT_BELONGS_TO_CIF("FUND_TRANSFER", "Account belongs to cif"),
    BALANCE_VALIDATION("FUND_TRANSFER", "Balance Validation"),
    BENEFICIARY_VALIDATION("FUND_TRANSFER", "Beneficiary validation"),
    CURRENCY_VALIDATION("FUND_TRANSFER", "Currency Validation"),
    FIN_TRANSACTION_VALIDATION("FUND_TRANSFER", "Financial Transaction Number Validation"),
    IBAN_VALIDATION("FUND_TRANSFER", "Iban Validation"),
    PAYMENT_PURPOSE_VALIDATION("FUND_TRANSFER", "Payment Purpose Validation"),
    SAME_ACCOUNT_VALIDATION("FUND_TRANSFER", "Check account credit and deit account should not be same"),
    FUND_TRANSFER_MW_CALL("FUND_TRANSFER", "Call middleware for fund transfer"),
    QR_FUND_TRANSFER_MW_CALL("FUND_TRANSFER", "Call middleware for quick remit fund transfer"),

    IBAN_SEARCH_MW_CALL("FUND_TRANSFER_ENQUIRY","Get bank details using IBAN"),
    ROUTING_CODE_SEARCH_MW_CALL("FUND_TRANSFER_ENQUIRY","Get bank details using Routing code"),
    IFSC_SEARCH_MW_CALL("FUND_TRANSFER_ENQUIRY","Get bank details using IFSC code"),
    CARD_LESS_CASH_MOBILE_NUMBER_DOES_NOT_MATCH("CARDLESS-CASH","Mobile Number is not valid"),
    CARD_LESS_CASH_ACCOUNT_NUMBER_DOES_NOT_MATCH("CARDLESS-CASH","Account Number is not valid"),
    CARD_LESS_CASH_OTP_DOES_NOT_MATCH("CARDLESS-CASH","OTP is not valid"),
    ;


    private static final Map<String, FundTransferEventType> eventTypeLookup = Stream.of(FundTransferEventType.values())
            .collect(Collectors.toMap(FundTransferEventType::name, eventType -> eventType));

    public static FundTransferEventType getEventTypeByCode(String code) {
        if (!eventTypeLookup.containsKey(code))
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_EVENT_TYPE_CODE, String.format("No Event type found for requested code [ %s ]", code));
        return eventTypeLookup.get(code);
    }

    private String type;
    private String description;

    FundTransferEventType(String type, String description) {
        this.type = type;
        this.description = description;
    }


}
