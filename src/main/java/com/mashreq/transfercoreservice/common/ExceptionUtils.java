package com.mashreq.transfercoreservice.common;

import com.mashreq.ms.exceptions.GenericBusinessException;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

	public static GenericException genericException(TransferErrorCode errorCode) {
		GenericBusinessException genericBusinessException = new GenericBusinessException(errorCode.getErrorMessage());
		return new GenericException(genericBusinessException, errorCode, null);
	}

	public static GenericException genericException(TransferErrorCode errorCode, String errorDetails) {
		GenericBusinessException genericBusinessException = new GenericBusinessException(errorCode.getErrorMessage());
		return new GenericException(genericBusinessException, errorCode, errorDetails);
	}

	public static GenericException genericException(String errorDetails) {
		GenericBusinessException genericBusinessException = new GenericBusinessException(errorDetails);
		return new GenericException(genericBusinessException, null, errorDetails);
	}
}