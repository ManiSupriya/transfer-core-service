package com.mashreq.transfercoreservice.infrastructure.web;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String EXP_MSG = "Exception occurred: {}";
	@Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                               HttpHeaders headers, HttpStatus status,
                                                               WebRequest request) {
	        logError(ex);
	        TransferErrorCode errorCode = TransferErrorCode.TRNS_CORE_400;
	        String errorMessage = null;
	        for (ObjectError fieldError : ex.getBindingResult().getGlobalErrors()) {
	            errorMessage = fieldError.getDefaultMessage();
	        }
	        for(FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
	            errorMessage = fieldError.getDefaultMessage();
	        }
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Response.builder().errorCode(errorCode.getCustomErrorCode()).message(errorCode.getErrorMessage()).errorDetails(errorMessage).build());
	    }
	
	@Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatus status, WebRequest request) {
        logError(ex);
        TransferErrorCode errorCode = TransferErrorCode.TRNS_CORE_406;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.builder().errorCode(errorCode.getCustomErrorCode()).message(errorCode.getErrorMessage()).errorDetails(ex.getMessage()).build());
    }
	/**
     * Log error
     */
    private <T extends Exception> void logError(T ex) {
        log.error(EXP_MSG, htmlEscape(ex.getMessage()));
    }
}
