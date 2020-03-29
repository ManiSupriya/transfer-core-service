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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TransferErrorCode implements ErrorCode {

    BENE_NOT_FOUND("TN-4001", "Beneficiary Not Found"),
    BENE_ACC_NOT_MATCH("TN-4002", "Beneficiary Account Number does not match"),
    BENE_CUR_NOT_MATCH("TN-4003", "Beneficiary Currency does not match"),
    BENE_NOT_ACTIVE("TN-4004", "Beneficiary is not active"),

    INVALID_PAYMENT_OPTIONS("TN-4005", "Invalid Payment Option Mode"),
    HEADER_MISSING_CIF("TN-4006", "CIF-ID is missing in Header"),
    INVALID_CIF("TN-4007", "CIF is invalid"),
    DUPLICATION_FUND_TRANSFER_REQUEST("TN-4008", "Duplicate Fund Transfer Request"),

    LIMIT_PACKAGE_NOT_FOUND("TN-4009", "Limit Package not Found"),
    TRX_LIMIT_REACHED("TN-4010", "Transaction Limit Reached"),
    DAY_AMOUNT_LIMIT_REACHED("TN-4011", "Day Amount Limit Reached"),
    DAY_COUNT_LIMIT_REACHED("TN-4012", "Day count Limit Reached"),
    MONTH_AMOUNT_LIMIT_REACHED("TN-4013", "Month Amount Limit Reached"),
    MONTH_COUNT_LIMIT_REACHED("TN-4014", "Month Count Limit Reached"),


    IBAN_NOT_FOUND("TN-4015", "IBAN not found"),
    ROUTING_CODE_NOT_FOUND("TN-4016", "Routing Code not found"),
    INVALID_ROUTING_CODE("TN-4017", "Invalid Routing Option Mode"),
    INVALID_COUNTRY_CODE("TN-4018", "Invalid Country Code"),
    IBAN_LENGTH_NOT_VALID("TN-4019", "IBAN should be 23 characters long"),
    SAME_BANK_IBAN("TN-4020", "Beneficiary Bank is same as sender bank"),

    CURRENCY_CONVERSION_FAIL("TN-4021", "Currency conversion failed."),
    BALANCE_NOT_SUFFICIENT("TN-4022", "Balance is not sufficient"),

    ACCOUNT_NOT_BELONG_TO_CIF("TN-4023", "Account does not belong to CIF"),
    CREDIT_AND_DEBIT_ACC_SAME("TN-4024", "Credit and Debit Account cannot be same"),
    TO_ACCOUNT_SHOULD_BE_DIFFERENT_CIF("TN-4025", "To Account should belong to a different CIF"),
    ACCOUNT_CURRENCY_MISMATCH("TN-4026", "Account currency does not match with given currency"),
    TO_ACCOUNT_BELONGS_TO_SAME_CIF("TN-4027", "To Account belongs to same CIF"),
    CURRENCY_IS_INVALID("TN-4028", "Currency does not match"),
    INVALID_PURPOSE_CODE("TN-4029", "Invalid purpose code"),
    INVALID_PURPOSE_DESC("TN-4030", "Invalid purpose description"),
    INTERNAL_SERVICE_ERROR("TN-5000", "Something went wrong"),
    FUND_TRANSFER_FAILED("TN-5001", "Fund transfer failed");

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
