package com.mashreq.transfercoreservice.client.service;


import static com.mashreq.transfercoreservice.client.ErrorUtils.getAllErrorCodesFromGenericException;
import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorHandlingStrategy;
import static com.mashreq.transfercoreservice.common.CommonConstants.*;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

import java.util.Map;
import java.util.Optional;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import feign.RetryableException;

public interface CoreEnquiryService<R, P> {


		Response<R> doCall(P p);

	    Map<String, String> errorMap();

	    Response<R> defaultSuccessResponse();

    TransferErrorCode assignDefaultErrorCode();
    
    TransferErrorCode assignFeignConnectionErrorCode();
    
    void assignCustomErrorCode(String errorDetails, TransferErrorCode errorCode);

    default Response<R> getResponse(P p) {
        try {
            return responseHandler(doCall(p));
        } catch (Exception ex) {
            return handleErrorResponse(ex);
        }
    }

    default Response<R> responseHandler(Response<R> response) {
        /**
         * error response
         */
    	
        if (ResponseStatus.ERROR == response.getStatus() || null == response.getData()) {
            final Optional<String> errorHandlingStrategyOptional = getErrorHandlingStrategy(errorMap(), response.getErrorCode());
            if (errorHandlingStrategyOptional.isPresent()) {
                final String errorHandlingStrategy = errorHandlingStrategyOptional.get();
                if (SEND_EMPTY_ERROR_RESPONSE.equals(errorHandlingStrategy)) {
                    return defaultSuccessResponse();
                } else if (INVALID_SESSION_TOKEN.equals(errorHandlingStrategy)) {
                	assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_INVALID_SESSION_TOKEN);
				} else if (OTP_VERIFIVATION_FAILED.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_OTP_FAILED);
				} else if (MAX_OTP_ATTEMPTS_EXCEEDED.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_ATTEMPTS_EXCEEDED);
				} else if (MAX_OTP_FAILED_ATTEMPTS_EXCEEDED.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_FAILED_ATTEMPTS_EXCEEDED);
				} else if (FAILED_TO_VERIFY_OTP.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_FAILED_TO_VERIFY);
				} else if (NOT_FOUND_USER_IN_DB.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_NOT_FOUND_USER_IN_DB);
				} else if (USER_BLOCKED_STATUS.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_USER_BLOCKED_STATUS);
				} else if (USER_INACTIVE_STATUS.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_USER_INACTIVE_STATUS);
				} else if (FAILED_TO_DECRYPT.equals(errorHandlingStrategy)) {
					assignCustomErrorCode(response.getErrorCode(), OTP_VERIFY_USER_FAILED_TO_DECRYPT);
				} else {
                    TransferErrorCode errorCode = TransferErrorCode.valueOf(errorHandlingStrategy);
                    GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage(), getErrorDetails(response));
                }
            } else {
                GenericExceptionHandler.handleError(assignDefaultErrorCode(), assignDefaultErrorCode().getErrorMessage(), getErrorDetails(response));
            }
        }

        /**
         * success response
         */
        return response;
    }


    default Response<R> handleErrorResponse(Throwable throwable) {
        if (throwable instanceof GenericException) {
            GenericException genericException = (GenericException) throwable;
            final String[] errorCodes = getAllErrorCodesFromGenericException(genericException);
            final String errorDetails = genericException.getErrorDetails();
            final Optional<String> errorHandlingStrategyOptional = getErrorHandlingStrategy(errorMap(), errorCodes);
            final String errorHandlingStrategy = errorHandlingStrategyOptional.get();

            if (errorHandlingStrategyOptional.isPresent()) {
                if (SEND_EMPTY_ERROR_RESPONSE.equals(errorHandlingStrategy)) {
                    return defaultSuccessResponse();
                } 
                else if (EXCEEDS_WITHDRAWL_FREEQUENCY.equals(errorHandlingStrategy)) {
                	GenericExceptionHandler.handleError(ACC_SERVICE_EXCEED_WITHDRAWL_ERROR, ACC_SERVICE_EXCEED_WITHDRAWL_ERROR.getErrorMessage(), errorDetails);
                } else if (EXCEEDS_WITHDRAWL_FREEQUENCY.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(ACC_SERVICE_EXCEED_WITHDRAWL_ERROR,
							ACC_SERVICE_EXCEED_WITHDRAWL_ERROR.getErrorMessage(), errorDetails);
				} else if (INVALID_SESSION_TOKEN.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_INVALID_SESSION_TOKEN,
							OTP_VERIFY_INVALID_SESSION_TOKEN.getErrorMessage(), errorDetails);
				} else if (OTP_VERIFIVATION_FAILED.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_OTP_FAILED,
							OTP_VERIFY_OTP_FAILED.getErrorMessage(), errorDetails);
				} else if (MAX_OTP_ATTEMPTS_EXCEEDED.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_ATTEMPTS_EXCEEDED,
							OTP_VERIFY_ATTEMPTS_EXCEEDED.getErrorMessage(), errorDetails);
				} else if (MAX_OTP_FAILED_ATTEMPTS_EXCEEDED.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_FAILED_ATTEMPTS_EXCEEDED,
							OTP_VERIFY_FAILED_ATTEMPTS_EXCEEDED.getErrorMessage(), errorDetails);
				} else if (FAILED_TO_VERIFY_OTP.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_FAILED_TO_VERIFY,
							OTP_VERIFY_FAILED_TO_VERIFY.getErrorMessage(), errorDetails);
				} else if (NOT_FOUND_USER_IN_DB.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_NOT_FOUND_USER_IN_DB,
							OTP_VERIFY_NOT_FOUND_USER_IN_DB.getErrorMessage(), errorDetails);
				} else if (USER_BLOCKED_STATUS.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_USER_BLOCKED_STATUS,
							OTP_VERIFY_USER_BLOCKED_STATUS.getErrorMessage(), errorDetails);
				} else if (USER_INACTIVE_STATUS.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_USER_INACTIVE_STATUS,
							OTP_VERIFY_USER_INACTIVE_STATUS.getErrorMessage(), errorDetails);
				} else if (FAILED_TO_DECRYPT.equals(errorHandlingStrategy)) {
					GenericExceptionHandler.handleError(OTP_VERIFY_USER_FAILED_TO_DECRYPT,
							OTP_VERIFY_USER_FAILED_TO_DECRYPT.getErrorMessage(), errorDetails);
				} else if (EXCEEDS_WITHDRAWL_LIMIT.equals(errorHandlingStrategy)) {
		            	GenericExceptionHandler.handleError(TransferErrorCode.ACC_SERVICE_EXCEED_WITHDRAWL_LIMIT_ERROR, TransferErrorCode.ACC_SERVICE_EXCEED_WITHDRAWL_LIMIT_ERROR.getErrorMessage(), errorDetails);
		        }else {
                    TransferErrorCode errorCode = TransferErrorCode.valueOf(errorHandlingStrategy);
                    GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage(), errorDetails);
                }
            }
           /**
            * when no mapping present send
            */
            GenericExceptionHandler.handleError(assignDefaultErrorCode(), assignDefaultErrorCode().getErrorMessage(), errorDetails);
        }
      /**
       * when connection failed for external service
       */
        if(throwable instanceof RetryableException) {
        	GenericExceptionHandler.handleError(assignFeignConnectionErrorCode(), assignFeignConnectionErrorCode().getErrorMessage(), assignFeignConnectionErrorCode().getErrorMessage());
        }
        GenericExceptionHandler.handleError(assignDefaultErrorCode(), assignDefaultErrorCode().getErrorMessage());
        return null;
    }
    


}