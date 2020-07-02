package com.mashreq.transfercoreservice.client.service;


import static com.mashreq.transfercoreservice.client.ErrorUtils.getAllErrorCodesFromGenericException;
import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorHandlingStrategy;
import static com.mashreq.transfercoreservice.common.CommonConstants.SEND_EMPTY_ERROR_RESPONSE;

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

    default Response<R> getResponse(P p) {
        try {
            return responseHandler(doCall(p));
        } catch (Exception ex) {
            return handleErrorResponse(ex);
        }
    }

    default Response<R> responseHandler(Response<R> response) {
        //error response
    	
        if (ResponseStatus.ERROR == response.getStatus() || null == response.getData()) {
            final Optional<String> errorHandlingStrategyOptional = getErrorHandlingStrategy(errorMap(), response.getErrorCode());
            if (errorHandlingStrategyOptional.isPresent()) {
                final String errorHandlingStrategy = errorHandlingStrategyOptional.get();
                if (SEND_EMPTY_ERROR_RESPONSE.equals(errorHandlingStrategy)) {
                    return defaultSuccessResponse();
                } else {
                    TransferErrorCode errorCode = TransferErrorCode.valueOf(errorHandlingStrategy);
                    GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage(), getErrorDetails(response));
                }
            } else {
                GenericExceptionHandler.handleError(assignDefaultErrorCode(), assignDefaultErrorCode().getErrorMessage(), getErrorDetails(response));
            }
        }

        //success response
        return response;
    }


    default Response<R> handleErrorResponse(Throwable throwable) {
        if (throwable instanceof GenericException) {
            GenericException genericException = (GenericException) throwable;
            final String[] errorCodes = getAllErrorCodesFromGenericException(genericException);
            final String errorDetails = genericException.getErrorDetails();
            final Optional<String> errorHandlingStrategyOptional = getErrorHandlingStrategy(errorMap(), errorCodes);

            if (errorHandlingStrategyOptional.isPresent()) {
                final String errorHandlingStrategy = errorHandlingStrategyOptional.get();
                if (SEND_EMPTY_ERROR_RESPONSE.equals(errorHandlingStrategy)) {
                    return defaultSuccessResponse();
                } else {
                    TransferErrorCode errorCode = TransferErrorCode.valueOf(errorHandlingStrategy);
                    GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage(), errorDetails);
                }
            }
            //when no mapping present send
            GenericExceptionHandler.handleError(assignDefaultErrorCode(), assignDefaultErrorCode().getErrorMessage(), errorDetails);
        }
      //when connection failed for external service
        if(throwable instanceof RetryableException) {
        	throwable.printStackTrace();
        	GenericExceptionHandler.handleError(assignFeignConnectionErrorCode(), assignFeignConnectionErrorCode().getErrorMessage(), assignFeignConnectionErrorCode().getErrorMessage());
        }
        throwable.printStackTrace();
        GenericExceptionHandler.handleError(assignDefaultErrorCode(), assignDefaultErrorCode().getErrorMessage());
        return null;
    }
    


}