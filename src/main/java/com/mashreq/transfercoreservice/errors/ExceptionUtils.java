package com.mashreq.transfercoreservice.errors;

import com.mashreq.ms.exceptions.GenericBusinessException;
import com.mashreq.ms.exceptions.GenericException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author shahbazkh
 * @date 10/21/20
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

    public static GenericException genericException(TransferErrorCode errorCode) {
        GenericBusinessException genericBusinessException = new GenericBusinessException(errorCode.getErrorMessage());
        return new GenericException(
                genericBusinessException,
                errorCode,
                null);
    }

    public static GenericException genericException(TransferErrorCode errorCode, String errorDetails) {
        GenericBusinessException genericBusinessException = new GenericBusinessException(errorCode.getErrorMessage());
        return new GenericException(
                genericBusinessException,
                errorCode,
                errorDetails);
    }
}
