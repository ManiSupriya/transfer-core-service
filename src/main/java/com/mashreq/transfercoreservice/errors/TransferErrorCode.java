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


    FROM_ACCOUNT_IS_INVALID("TNF-ESB-13239", "From Account is Invalid"),
    FROM_ACCOUNT_IS_NON_ACTIVE("TNF-CORE-412", "From Account is non active"),
    TO_ACCOUNT_IS_INVALID("TNF-ACC-5001", "To Account is Invalid"),
    NOT_ENOUGH_RESOURCES("TNF-CORE-412", "Not enough resources in account"),
    SAME_DEBIT_CREDIT_ACC("TNF-ESB-2262", "Same Debit and Credit Account"),
    INVALID_REQ_BODY("ACC-CORE-400","Please validate your request body"),


    INVALID_PAYMENT_OPTIONS("TNF-API-001", "Invalid Payment Option Mode"),
    HEADER_MISSING_CIF("TNF-API-002", "CIF-ID is missing in Header"),
    INVALID_CIF("TNF-API-003", "CIF is invalid"),
    DUPLICATION_FUND_TRANSFER_REQUEST("TNF-API-004", "Duplicate Fund Transfer Request"),


    LIMIT_PACKAGE_NOT_FOUND("TNF-API-005", "Limit Package not Found"),
    TRX_LIMIT_REACHED("TNF-API-006", "Transaction Limit Reached"),
    DAY_AMOUNT_LIMIT_REACHED("TNF-API-007", "Day Amount Limit Reached"),
    DAY_COUNT_LIMIT_REACHED("TNF-API-008", "Day count Limit Reached"),
    MONTH_AMOUNT_LIMIT_REACHED("TNF-API-009", "Month Amount Limit Reached"),
    MONTH_COUNT_LIMIT_REACHED("TNF-API-010", "Mont Count Limit Reached");
    //PAYMENT_FAILURE("PAYMENT-003-PAYMENT-FAILURE", "Payment Failed for bill ref number %s");

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
