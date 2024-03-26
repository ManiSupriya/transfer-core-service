package com.mashreq.transfercoreservice.cardlesscash.constants;

import com.mashreq.ms.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CardlessCashErrorCode implements ErrorCode {

    // Common error codes
    SEGMENT_NOT_FOUND("CS-1012", "Digital user segment not found");


    private String errorCode;
    private String message;

    CardlessCashErrorCode(String code, String message) {
        this.errorCode = code;
        this.message = message;
    }

    @Override
    public String customErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public static CardlessCashErrorCode getMobCustomerErrorCode(String name) {
        return Stream.of(CardlessCashErrorCode.values())
                .filter(type -> type.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
