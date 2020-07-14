package com.mashreq.transfercoreservice.errors;

import com.mashreq.ms.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    ACCOUNT_NUMBER_VALIDATION_FAILED_TO_CIF("TN-1009", "Account Number validation failed to CIF for User Session"),


    BALANCE_NOT_SUFFICIENT("TN-1200", "Balance is not sufficient"),

    LIMIT_PACKAGE_NOT_FOUND("TN-1101", "Limit Package not Found"),
    TRX_LIMIT_REACHED("TN-1102", "Transaction Limit Reached"),
    DAY_AMOUNT_LIMIT_REACHED("TN-1103", "Day Amount Limit Reached"),
    DAY_COUNT_LIMIT_REACHED("TN-1104", "Day count Limit Reached"),
    MONTH_AMOUNT_LIMIT_REACHED("TN-1104", "Month Amount Limit Reached"),
    MONTH_COUNT_LIMIT_REACHED("TN-1105", "Month Count Limit Reached"),

    BENE_NOT_FOUND("TN-4001", "Beneficiary Not Found"),
    BENE_ACC_NOT_MATCH("TN-4002", "Beneficiary Account Number does not match"),
    BENE_CUR_NOT_MATCH("TN-4003", "Beneficiary Currency does not match"),
    BENE_NOT_ACTIVE("TN-4004", "Beneficiary status is not active"),
    BENE_NOT_ACTIVE_OR_COOLING("TN-4004", "Beneficiary status is not active/cooling"),

    CURRENCY_CONVERSION_FAIL("TN-4100", "Currency conversion failed."),
    INVALID_PURPOSE_CODE("TN-4101", "Invalid purpose code"),
    INVALID_PURPOSE_DESC("TN-4102", "Invalid purpose description"),


    ACCOUNT_NOT_BELONG_TO_CIF("TN-4200", "Account does not belong to CIF"),
    ACCOUNT_CURRENCY_MISMATCH("TN-4201", "Account currency does not match with given currency"),
    CURRENCY_IS_INVALID("TN-4202", "Currency does not match"),
    ACCOUNT_NOT_FOUND("TN-4003", "Account not found"),
    CONNECTED_ACCOUNT_EMPTY("TN-4004", "Connected Account is empty"),

    IBAN_NOT_FOUND("TN-5100", "IBAN not found"),
    ROUTING_CODE_NOT_FOUND("TN-5102", "Routing Code not found"),
    INVALID_ROUTING_CODE("TN-5103", "Invalid Routing Option Mode"),
    INVALID_COUNTRY_CODE("TN-5104", "Invalid Country Code"),
    IBAN_LENGTH_NOT_VALID("TN-5105", "IBAN length is not valid"),
    SAME_BANK_IBAN("TN-5106", "Beneficiary Bank is same as sender bank"),
    ROUTING_CODE_EMPTY("TN-5107", "Beneficiary routing code cannot be empty"),
    ROUTING_CODE_LENGTH_INVALID("TN-5108", "Beneficiary routing code length is invalid"),
    SWIFT_CODE_EMPTY("TN-5109", "Beneficiary SWIFT code cannot be empty"),
    IFSC_CODE_NOT_FOUND("TN-5110", "IFSC Code not found"),
    INVALID_FLEX_RULE_COUNTRY("TN-5111", "Invalid Country for rule engine"),


    EXTERNAL_SERVICE_ERROR("TN-5000", "Something went wrong with external service"),
    FUND_TRANSFER_FAILED("TN-5001", "Fund transfer failed"),
    FUND_TRANSFER_PROCESSING("TN-5004", "Fund transfer is in processing"),

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
    OTP_VERIFY_INVALID_SESSION_TOKEN("TN-5018", "OTP external service Invalid session token exception"),
    OTP_VERIFY_OTP_FAILED("TN-5019", "OTP external service Otp verification failed exception"),
    OTP_VERIFY_ATTEMPTS_EXCEEDED("TN-5020", "otp verification attempts exceeded exception"),
    OTP_VERIFY_FAILED_ATTEMPTS_EXCEEDED("TN-5021", "Max otp verification failed attempt is reached exception"),
    OTP_VERIFY_FAILED_TO_VERIFY("TN-5022", "Failed to verify OTP in OTP exception"),
    OTP_VERIFY_NOT_FOUND_USER_IN_DB("TN-5023", "Could not find user in database exception"),
    OTP_VERIFY_USER_BLOCKED_STATUS("TN-5024", "User is in blocked status in database. Operation not allowed"),
    OTP_VERIFY_USER_INACTIVE_STATUS("TN-5025", "User is in inactive status in database. Operation not allowed"),

    //Middleware error codes
    CONNECTION_TIMEOUT_MW("TN-8002", "Middleware Connection Timeout"),
    INTERNAL_ERROR("TN-8004", "Something went wrong"),
    EXTERNAL_SERVICE_ERROR_MW("TN-8003", "Something went wrong with middleware service"),
    INVALID_EVENT_TYPE_CODE("TN-8004","Invalid event code");


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
