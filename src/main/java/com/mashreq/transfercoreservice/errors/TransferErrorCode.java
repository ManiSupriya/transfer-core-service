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
    HEADER_MISSING_CIF("TRANS-4000", "CIF-ID is missing in Header");

    private final String customName;
    private final String message;

    @Override
    public String customName() {
        return customName;
    }
}
