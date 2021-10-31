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
    CREDIT_CARD_FUND_TRANSFER_REQUEST("FUND_TRANSFER", "Credit card transaction request received"),
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
    CARD_LESS_CASH_BALANCE_VALIDATION_SUCCESS("CARDLESS-CASH", "Balance Validation Success"),
    CARD_LESS_CASH_BALANCE_VALIDATION("CARDLESS-CASH", "Balance Validation Failed"),
    SWIFT_GPI_TRANSACTION_DETAILS("SWIFT-TRANSACTION", "Get Swift Transaction Details"),
    GET_GPI_TRANSACTION_DETAILS("GPI-TRANSACTION", "Get Swift Message Details"),
    LOYALTY_SMILE_CARD_GEN_DETAILS("LOYALTY-SMILE-CARD", "Loyalty smile-card Generation Reedem Message Details"),
    LOYALTY_SMILE_CARD_VAL_DETAILS("LOYALTY-SMILE-CARD", "Loyalty smile-card Validate Redeem Message Details"),
    LOYALTY_SMILE_CARD_ERROR("LOYALTY-SMILE-CARD", "Loyalty smile-card Validation failed"),

    CHARITY_ACCOUNT_VALIDATION("FUND_TRANSFER", "Charity Account Validation"),
    ACCOUNT_BELONGS_TO_CIF("FUND_TRANSFER", "Account belongs to cif"),
    ACCOUNT_IS_DORMENT("FUND_TRANSFER", "Account is dorment"),
    ACCOUNT_IS_UNDER_DEBIT_FREEZE("FUND_TRANSFER", "Account is frozen for debit"),
    ACCOUNT_IS_UNDER_CREDIT_FREEZE("FUND_TRANSFER", "Account is frozen for credit"),
    ACCOUNT_FREEZE_VALIDATION("FUND_TRANSFER", "Account belongs to cif"),
    BALANCE_VALIDATION("FUND_TRANSFER", "Balance Validation"),
    DEAL_VALIDATION("FUND_TRANSFER", "Deal Validation"),
    BENEFICIARY_VALIDATION("FUND_TRANSFER", "Beneficiary validation"),
    FUNDTRANSFER_BENDETAILS("FUND_TRANSFER", "Ben detail retrieval failed"),
    CURRENCY_VALIDATION("FUND_TRANSFER", "Currency Validation"),
    FIN_TRANSACTION_VALIDATION("FUND_TRANSFER", "Financial Transaction Number Validation"),
    IBAN_VALIDATION("FUND_TRANSFER", "Iban Validation"),
    RESOURCE_NOT_MACTH("FUND_TRANSFER","There is not enough resources in account"),
    PAYMENT_PURPOSE_VALIDATION("FUND_TRANSFER", "Payment Purpose Validation"),
    SAME_ACCOUNT_VALIDATION("FUND_TRANSFER", "Check account credit and deit account should not be same"),
    FUND_TRANSFER_MW_CALL("FUND_TRANSFER", "Call middleware for fund transfer"),
    FUND_TRANSFER_CC_MW_CALL("FUND_TRANSFER", "Call middleware for fund transfer Credit card as source of fund"),
    FUND_TRANSFER_CC_CALL("FUND_TRANSFER", "Fund transfer Credit card as source of fund"),
    QR_FUND_TRANSFER_MW_CALL("FUND_TRANSFER", "Call middleware for quick remit fund transfer"),
    LIMIT_VALIDATION("FUND_TRANSFER", "Call mob common to validate the spend limit"),
    MIN_LIMIT_VALIDATION("FUND_TRANSFER", "Call serviceType to get min amount"),
    LIMIT_CHECK_FAILED("CARDLESS-CASH", "CARDLESS-CASH limit check failed"),
    LIMIT_CHECK_SUCCESS("CARDLESS-CASH", "CARDLESS-CASH limit check success"),

    IBAN_SEARCH_MW_CALL("FUND_TRANSFER_ENQUIRY","Get bank details using IBAN"),
    ROUTING_CODE_SEARCH_MW_CALL("FUND_TRANSFER_ENQUIRY","Get bank details using Routing code"),
    IFSC_SEARCH_MW_CALL("FUND_TRANSFER_ENQUIRY","Get bank details using IFSC code"),
    CARD_LESS_CASH_MOBILE_NUMBER_DOES_NOT_MATCH("CARDLESS-CASH","Mobile Number is not valid"),
    CARD_LESS_CASH_ACCOUNT_NUMBER_DOES_NOT_MATCH("CARDLESS-CASH","Account Number is not valid"),
    CARD_LESS_CASH_OTP_DOES_NOT_MATCH("CARDLESS-CASH","OTP is not valid"),
    CARD_LESS_CASH_OTP_VALIDATION("CARDLESS-CASH","OTP validation is success"),
    FUND_TRANSFER_OTP_DOES_NOT_MATCH("FUND_TRANSFER","OTP is not valid"),
    FUND_TRANSFER_OTP_VALIDATION("FUND_TRANSFER","OTP validation is success"),
    CARD_LESS_CASH_REFERENCE_NO_INVALID("CARDLESS-CASH","Not a valid Reference Number for the account"),

    FLEX_GET_EXCHANGE_RATE("FUND_TRANSFER_ENQUIRY", "Get exchange rate for QR"),
    FLEX_GET_CHARGES("FUND_TRANSFER_ENQUIRY", "Get charges for QR"),
    FLEX_RULE_ENGINE_MW_CALL("FUND_TRANSFER_ENQUIRY","Flex rule engine MW call"),
    SMS_NOTIFICATION("SMS_NOTIFICATION","Send Sms for transfer-core"),
    PUSH_NOTIFICATION("PUSH_NOTIFICATION","Send push for transfer-core"),
    EMAIL_NOTIFICATION("EMAIL_NOTIFICATION","Send Email for transfer-core"),
    APPLICATION_SETTING_KEY_NOT_FOUND("APPLICATION_SETTING_KEY_NOT_FOUND","Application setting key is not found"),
    BIC_LIST_SEARCH_CALL("FUND_TRANSFER_ENQUIRY", "Bic code list for country code"),
    ELIGIBILITY_QUICK_REMIT_EXCHANGE("FUND_TRANSFER_ELIGIBILITY", "Quick remit eligibility"),
    FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED("FUNDS_TRANSFER_TERMSANDCONDITIONS_ACCEPTED", "Terms and Conditions for Funds Transfer accepted"),
    SET_VALUE_IN_CACHE("FUND_TRANSFER_ELIGIBILITY", "Set value to cache when value not found in cache ")
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
