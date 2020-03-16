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
    INVALID_REQ_BODY("TNF-CORE-400", "Please validate your request body"),
    REQ_PARAM_MISSING("TNF_CORE_406", "Request parameter is missing"),
    NO_HANDLER_FOUND("TNF-CORE-407", "Handler not found for requested method"),
    SOMETHING_WRONG_IN_ACCOUNT_SEVICE("TNF-CORE-999", "Something went wrong"),
    QACLNTN_EAI_FCI_BRK_115("QACLNTN-EAI-FCI-BRK-115", "No data found for given input"),
    TFTN_EAI_FCI_BRK_2471("TFTN-EAI-FCI-BRK-2471", "Debit Amount Cannot be less than or equal to Zero,Credit Amount " +
            "Cannot be less than or equal to Zero"),
    ACC_ESB_TIMEOUT("TNF-ESB-TIMEOUT", "Esb response timeouts"),


    BENE_NOT_FOUND("TNF-BEN-001", "Beneficiary Not Found"),
    BENE_ACC_NOT_MATCH("TNF-BEN-002", "Beneficiary Account Number does not match"),
    BENE_CUR_NOT_MATCH("TNF-BEN-003", "Beneficiary Currency does not match"),


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
