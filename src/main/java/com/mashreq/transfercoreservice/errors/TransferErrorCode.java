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

    INVALID_PAYMENT_OPTIONS("TRANS-4000", "Invalid Payment Option Mode"),
    HEADER_MISSING_CIF("TRANS-4001", "CIF-ID is missing in Header"),
    INVALID_CIF("TRANS-4002", "CIF is invalid"),
    LIMIT_PACKAGE_NOT_FOUND("LIMIT_PACKAGE_NOT_FOUND", ""),
    TRX_LIMIT_REACHED("TRX_LIMIT_REACHED", ""),
    DAY_AMOUNT_LIMIT_REACHED("DAY_AMOUNT_LIMIT_REACHED", ""),
    DAY_COUNT_LIMIT_REACHED("DAY_COUNT_LIMIT_REACHED", ""),
    MONTH_AMOUNT_LIMIT_REACHED("MONTH_AMOUNT_LIMIT_REACHED", ""),
    MONTH_COUNT_LIMIT_REACHED("MONTH_COUNT_LIMIT_REACHED", ""),
    PAYMENT_FAILURE("PAYMENT-003-PAYMENT-FAILURE", "Payment Failed for bill ref number %s");

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
