package com.mashreq.transfercoreservice.errors;

import com.mashreq.ms.exceptions.ErrorCode;
import lombok.Getter;

/**
 * @author shahbazkh
 * @date 3/9/20
 */
@Getter
public enum TransferErrorCode implements ErrorCode {

    INVALID_PAYMENT_OPTIONS("TN-1000", "Invalid Payment Option Mode"),
    HEADER_MISSING_CIF("TN-1001", "CIF-ID is missing in Header"),
    INVALID_CIF("TN-1002", "CIF is invalid"),
    DUPLICATION_FUND_TRANSFER_REQUEST("TN-1003", "Duplicate Fund Transfer Request"),
    CREDIT_AND_DEBIT_ACC_SAME("TN-1004", "Credit and Debit Account cannot be same"),
    INVALID_REQUEST("TN-1005","Invalid tranfer request"),
    ACCOUNT_NUMBER_DOES_NOT_BELONG_TO_CIF("TN-1006", "Account Number does not belong to CIF"),
    USER_SESSION_CONTEXT_NOT_FOUND("MD-1007", "User Session Context not found"),
    MOBILE_NUMBER_DOES_NOT_MATCH("TN-1008", "Mobile Number is not valid"),
    INVALID_DEAL_NUMBER("TN-1009", "Invalid Deal Number"),
    DEAL_NUMBER_EXPIRED("TN-1010", "Deal Number Expired"),
    DEAL_NUMBER_NOT_APPLICABLE("TN-1011", "Invalid Transaction, txn amount is greater than available deal amount"),
    MAINTENANCE_SERVICE_CONNECTION_ERROR("TN-1012", "Maintenance external service connection exception"),
    MAINTENANCE_SERVICE_ERROR("TN-1013", "Something went wrong with maintenance service"),
    DEAL_NUMBER_NOT_APPLICABLE_WITH_SAME_CRNCY("TN-1014", "Deal Number Not Applicable for same currency transaction"),
    INVALID_SESSION_ID("TN-1015", "Session Id is invalid"),
    EXPIRED_SESSION_ID("TN-1016", "Session Id is expired"),
    NOT_MATCHING_CARD_DETAILS("TN-1017", "Card details not found"),
    NOT_VALID_DATE_GPI_TRACKER("TN-1018", "Not a Valid date, startDate should Not be 6 months old"),
    NOT_VALID_END_DATE_GPI_TRACKER("TN-1019", "Not a Valid date, end date should be always greter than start date"),
    DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY("TN-1020", "Deal Number Not Applicable, buy currency not matching with account currency"),
    DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY("TN-1021", "Deal Number Not Applicable, sell currency not matching with trxn currency"),
    DEAL_NUMBER_NOT_AUTHORIRED("TN-1022", "Deal Number Not Authorised"),
    DEAL_NUMBER_NOT_VALID_STATE("TN-1023", "Deal Number Not in Valid State"),
    FX_CONTENET_ERROR("TN-1024","Content not found "),
    NOT_ABLE_TO_FETCH_GPI_TRACKER("TN-1025", "Not able to fetch the details from core"),
    DEAL_NUMBER_TOTALLY_UTILIZED("TN-1026", "The deal number completely utilized"),
    TRNS_CORE_400("TRNS-CORE-400", "Please validate your request body"),
    TRNS_CORE_406("TRNS-CORE-406", "Request parameter is missing"),
    TXN_CURRENCY_INVALID("TN-1027", "Not a valid transfer currency"),
    REFERENCE_NO_INVALID("TN-1028", "Not a valid Reference Number for the account"),
    DESTINATION_ACCOUNT_FREEZED_FOR_CREDIT("TN-1029", "Destination account is freezed for credit"),
    SOURCE_ACCOUNT_FREEZED_FOR_DEBIT("TN-1030", "Destination account is freezed for credit"),
    INVALID_FT_ORDER_TYPE("TN-1031", "Invalid funds tranfer order type"),
    INVALID_SI_FREQUENCY_TYPE("TN-1032", "Invalid frequency type"),
    TXN_NOT_ALLOWED_FOR_PAY_LATER("TN-1033", "Pay Later not allowed"),
    INVALID_CHARGE_BEARER("TN-1034", "Invalid Charge Bearer"),
    PAY_LATER_TRANSACTION_INITIATION_FAILED("TN-1035", "Transaction initiation failed"),
    INVALID_BEN_CODE("LM-1106","Invalid ben code"),

    DEAL_VALIDATION_FAILED("TN-1201", "Deal Number validation Failed"),
    BALANCE_NOT_SUFFICIENT("TN-1200", "Balance is not sufficient"),
    ERROR_LIMIT_CHECK("LM-2007", "Error during limit procedure call"),
    LIMIT_PACKAGE_NOT_DEFINED("LM-2008", "Limit Package not Found"),
    DAILY_COUNT_REACHED("LM-2018", "Daily usage count exceeded"),
    MONTHLY_COUNT_REACHED("LM-2009", "Monthly usage count exceeded"),
    COOLING_LIMIT_COUNT_REACHED("LM-2010", "Cooling period transaction count exceeded"),
    DAILY_AMOUNT_REACHED("LM-2011", "Daily usage amount exceeded"),
    MONTHLY_AMOUNT_REACHED("LM-2012", "Monthly usage amount exceeded"),
    TRX_AMOUNT_REACHED("LM-2013", "Transaction amount is greater than transaction max amount limit"),
    COOLING_LIMIT_AMOUNT_REACHED("LM-2014", "Cooling period transaction amount exceeded"),
    MIN_AMOUNT_LIMIT_REACHED("LM-2015", "Min Amount Limit Reached"),

    MONTH_AMOUNT_LIMIT_REACHED("PM-1104", "Month Amount Limit Reached"),
    DAY_AMOUNT_LIMIT_REACHED("PM-1103", "Day Amount Limit Reached"),

    BENE_NOT_FOUND("TN-4001", "Beneficiary Not Found"),
    BENE_ACC_NOT_MATCH("TN-4002", "Beneficiary Account Number does not match"),
    BENE_CUR_NOT_MATCH("TN-4003", "Beneficiary Currency does not match"),
    BENE_NOT_ACTIVE("TN-4004", "Beneficiary status is not active"),
    BENE_NOT_ACTIVE_OR_COOLING("TN-4004", "Beneficiary status is not active/cooling"),
    BEN_DETAIL_FAILED("TN-4005", "Failed to fetch ben details"),
    MISSING_BEN_DETAILS("TN-4006", "Required ben details are missing"),

    CURRENCY_CONVERSION_FAIL("TN-4100", "Currency conversion failed."),
    INVALID_PURPOSE_CODE("TN-4101", "Invalid payment purpose passed "),


    ACCOUNT_NOT_BELONG_TO_CIF("TN-4200", "Account does not belong to CIF"),
    ACCOUNT_IS_IN_DORMENT("TN-4203", "Account is dormant"),
    ACCOUNT_CURRENCY_MISMATCH("TN-4201", "Account currency does not match with given currency"),
    CURRENCY_IS_INVALID("TN-4202", "Currency does not match"),
    ACCOUNT_NOT_FOUND("TN-4003", "Account not found"),
    CONNECTED_ACCOUNT_EMPTY("TN-4004", "Connected Account is empty"),
    ACCOUNT_RESOURCES_MISMATCH("TN-4005", "There was not enough resources in account "),
    LOCAL_CURRENCY_MISMATCH("TN-4006", "Local currency does not match with transaction currency"),

    IBAN_NOT_FOUND("TN-5100", "IBAN not found"),
    ROUTING_CODE_NOT_FOUND("TN-5102", "Routing Code not found"),
    INVALID_ROUTING_CODE("TN-5103", "Invalid Routing Code"),
    INVALID_COUNTRY_CODE("TN-5104", "Invalid Country Code"),
    IBAN_LENGTH_NOT_VALID("TN-5105", "IBAN length is not valid"),
    SAME_BANK_IBAN("TN-5106", "Beneficiary Bank is same as sender bank"),
    ROUTING_CODE_EMPTY("TN-5107", "Beneficiary routing code cannot be empty"),
    ROUTING_CODE_LENGTH_INVALID("TN-5108", "Beneficiary routing code length is invalid"),
    SWIFT_CODE_EMPTY("TN-5109", "Beneficiary SWIFT code cannot be empty"),
    IFSC_CODE_NOT_FOUND("TN-5110", "IFSC Code not found"),
    INVALID_FLEX_RULE_COUNTRY("TN-5111", "Invalid Country for rule engine"),
    INVALID_SWIFT_CODE("TN-5112", "Invalid Swift code"),
    BIC_SEARCH_FAILED("TN-5113", "BIC Code search failed"),
    SWIFT_AND_BIC_SEARCH_FAILED("TN-5114", "Both Swift and BIC Code search failed"),

    EXTERNAL_SERVICE_ERROR("TN-5000", "Something went wrong with external service"),
    FUND_TRANSFER_FAILED("TN-5001", "Fund transfer failed"),
    FUND_TRANSFER_PROCESSING("TN-5004", "Fund transfer is in processing"),
    TRANSFER_AMOUNT_IN_NAGATIVE("TN-5006", "Middleware: Transfer amount will be in negative"),
    FUND_TRANSFER_FREEZED("TN-5007", "Middleware: Transfer Failed You cannot transfer funds from this account due to restrictions"),
    ULTIMATE_BENEFICIARY_IBAN_NOT_VALID("TN-5008", "Middleware: IBAN Check Digit validation failed,Ultimate Beneficiary IBAN is not valid"),
    EXACHANGE_RATE_AMOUNT_NOT_EQUATE("TN-5009", "Exchange rate Debit/Credit Amount does not equate"),
    MW_TRANSACTION_NOT_ALLOWED_FOR_PRODUCT("TN-5010","Middleware: Transaction is not allowed for this product. Kindly contact bank for more details"),
    MW_INSUFFICIENT_FUND("TN-5011","Middleware: Account does not have sufficient balance for this transaction to proceed." +
            " It might be due to charges in addition to the transaction amount"),
    MW_TRANSACTION_NOT_ALLOWED_FOR_TRANSACTION_CODE("TN-5012","Middleware: Transaction is not allowed for this transaction code. Kindly contact bank for more details"),
    MW_ACCOUNT_CLOSED("TN-5013","Middleware: Account is closed"),
    MW_RESTRICED_FOR_COUNTRY("TN-5014","Middleware: The beneficiary country is in Sanctioned Country List and restricted for the combination of DBFC and USD"),
    MW_DEBIT_FAILURE_FOR_KYC("TN-5015","Middleware: Debit Transaction Not allowed for account due to KYC expired."),
    MW_UNHANDLED_EXCEPTION("TN-5016","Middleware: Internal error, unhandled Exception Raised"),
    MW_FUTURE_VALUED_TRANSFER_NOT_ALLOWED("TN-5017","Future Valued Transfers not allowed"),
    MW_ACCOUNT_NOT_MARKED_FOR_CREDIT("TN-5018","Account marked for no credit"),
    MW_AMOUNT_FULLY_UTILIZED("TN-5019","Deal Amount is fully utilized, Transaction cannot be processed for the given Deal Ref No"),

    FLEX_RULE_ENGINE_FAILED("TN-6000", "Flex Rule Engine Failed"),
    FLEX_RULE_EITHER_DEBIT_OR_CREDIT_AMT_REQUIRED("TN-6001", "Either Debit or Credit Amount Required"),
    FLEX_RULE_ONLY_1_AMOUNT_ALLLOWED("TN-6002", "Only one Debit or Credit Amount allowed"),
    FLEX_RULE_MIN_TRANSACTION_VIOLATION("TN-6003", "Minimum Amount Transaction Violation"),
    FLEX_RULE_MAX_TRANSACTION_VIOLATION("TN-6004", "Maximum Amount Transaction Violation"),
    FLEX_RULE_NO_RATE_PAIRS_PRESENT("TN-6005", "No rates available for this pair"),
    FLEX_RULE_NO_AGGREGATOR_PRESENT("TN-6006", "No Aggregator Present"),
    FLEX_RULE_BIC_CODE_NOT_SUPPORTED("TN-6007", "Bic code not supported"),


    QUICK_REM_ROUTING_CODE_NOT_AVAILABLE("TN-7000", "Routing not available for the Beneficiary"),
    QUICK_REM_SWIFT_CODE_NOT_FOUND("TN-7001", "Swift Code not found for beneficiary"),
    QUICK_REM_COUNTRY_CODE_NOT_FOUND("TN-7002", "Country Code not found for beneficiary"),

    BENE_EXTERNAL_SERVICE_ERROR("TN-5010", "Something went wrong with beneficiary external service"),
    ACC_EXTERNAL_SERVICE_ERROR("TN-5011", "Something went wrong with account external service"),
    MOB_COM_EXTERNAL_SERVICE_ERROR("TN-5012", "Something went wrong with mob-common external service"),
    MAINTENANCE_COM_EXTERNAL_SERVICE_ERROR("TN-5013", "Something went wrong with maintenance external service"),
    ACC_SERVICE_CONNECTION_ERROR("TN-5014", "Account external service connection exception"),
    ACC_SERVICE_EXCEED_WITHDRAWL_ERROR("TN-5015", "Account external service exceeds withdrawal frequency exception"),
    ACC_SERVICE_EXCEED_WITHDRAWL_LIMIT_ERROR("TN-5016", "Account external service exceeds withdrawal limit exception"),
    OTP_EXTERNAL_SERVICE_ERROR("TN-5016", "Something went wrong with OTP external service"),
    OTP_SERVICE_CONNECTION_ERROR("TN-5017", "OTP external service connection exception"),
    OTP_VERIFY_INVALID_SESSION_TOKEN("VERIFY_OTP_4003", "OTP external service Invalid session token exception"),
    OTP_VERIFY_OTP_FAILED("VERIFY_OTP_10020", "OTP external service Otp verification failed exception"),
    OTP_VERIFY_ATTEMPTS_EXCEEDED("VERIFY_OTP_10200", "otp verification attempts exceeded exception"),
    OTP_VERIFY_FAILED_ATTEMPTS_EXCEEDED("VERIFY_OTP_10403", "Max otp verification failed attempt is reached exception"),
    OTP_VERIFY_FAILED_TO_VERIFY("VERIFY_OTP_USSM_100", "Failed to verify OTP in OTP exception"),
    OTP_VERIFY_NOT_FOUND_USER_IN_DB("VERIFY_OTP_USSM_101", "Could not find user in database exception"),
    OTP_VERIFY_USER_BLOCKED_STATUS("VERIFY_OTP_USSM_102", "User is in blocked status in database. Operation not allowed"),
    OTP_VERIFY_USER_INACTIVE_STATUS("VERIFY_OTP_USSM_103", "User is in inactive status in database. Operation not allowed"),
    OTP_VERIFY_USER_FAILED_TO_DECRYPT("VERIFY_OTP_USSM_104", "Failed to decrypt OTP exception."),
    DENIED_BY_POLICY_OTP_REUSE_NOT_ALLOWED_STAUS("VERIFY_OTP_10402", "Denied by policy otp reuse not allowed"),
    OBJ_TOKENSTORE_ID_NOT_FOUND_STATUS("VERIFY_OTP_30001", "Object TokenStore id not found"),
    USER_SESSION_ALREADY_INVALIDATED_STATUS("LOGOUT_4004", "User session already invalidated in IAM"),

    //Middleware error codes
    CONNECTION_TIMEOUT_MW("TN-8002", "Middleware Connection Timeout"),
    INTERNAL_ERROR("TN-8004", "Something went wrong"),
    EXTERNAL_SERVICE_ERROR_MW("TN-8003", "Something went wrong with middleware service"),
    INVALID_EVENT_TYPE_CODE("TN-8004","Invalid event code"),

    FT_CC_NOT_BELONG_TO_CIF("TN-8005", "CC does not belong to CIF"),
    FT_CC_MW_ERROR("TN-8006","Error is occurred while calling middleware for Fund Transfer via CC"),
    FT_CC_MW_EMPTY_RESPONSE("TN-8007","Null response from middleware for Fund Transfer via CC"),
    FT_CC_MW_ERROR_RESPONSE("TN-8008","Error response from middleware for Fund Transfer via CC"),
    FT_CC_NO_DEALS("TN-8008","No credit cards deals are available for the given cif"),
    FT_CC_BALANCE_NOT_SUFFICIENT("TN-8009","Limit exceeds for CC fund transfer"),
    SMS_NOTIFICATION_FAILED("TN-9001","Failed to send sms notification"),
    APPLICATION_KEY_NOT_FOUND("TN-9002","Application setting key not found"),
    PUSH_NOTIFICATION_FAILED("TN-9003","Failed to send push notification"),
    ACCOUNT_NO_NOT_MASKED("TN-8010","Account no masked is failed due to length"),
    EMAIL_NOTIFICATION_FAILED("EMAIL_9001","Failed to send email notification"),
    FT_CC_MW_SUCCESS_FAILED_RESPONSE("TN-8011","Transaction pending for processing"),
    
    INVALID_USER_TYPE("TN-8022", "Invalid User type"),
    
    QUICK_REMIT_EXTERNAL_SERVICE_ERROR("TN-8023", "Something went wrong with quick remit external service"),
    
    INVALID_SEGMENT("TN-8024", "Invalid segment"),
    
    PAYMENT_ELIGIBILITY_ERROR("TN-8025", "Something went wrong while checking payment eligibility"),
    
    IBAN_DETAILS_NOT_FOUND("TN-8026", "IBAN Account details not found"),
    IBAN_CHECK_DIGIT_VALIDATION_FAILED("TN-8027", "IBAN check digit validation failed"),
    INVALID_IBAN_LENGTH("TN-8028", "Invalid IBAN | Account number"),
    
    BANK_NOT_FOUND_WITH_IBAN("TN-8029", "Bank not found with iban"),
    PAYMENT_NOT_ELIGIBLE_FOR_QR("TN-8030", "Payment not eligible for quick remit"), 
    LOCAL_CURRENCY_NOT_ALLOWED_FOR_SWIFT("TN-8031", "AED currency not allowed for Swift transactions"),
    ACCOUNT_DEBIT_FREEZE("TN-8032", "Account is frozen for debit"),
    ACCOUNT_CREDIT_FREEZE("TN-8033", "Account is frozen for credit"),
    PAYMENT_NOT_ELIGIBLE("TN-8034", "Payment not eligible"),
    ;


    private String customErrorCode;
    private String errorMessage;

    TransferErrorCode(String customErrorCode, String errorMessage) {
        this.customErrorCode = customErrorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String customErrorCode() {
        return customErrorCode;
    }

}
