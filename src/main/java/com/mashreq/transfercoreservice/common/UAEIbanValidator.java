package com.mashreq.transfercoreservice.common;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.IBAN_CHECK_DIGIT_VALIDATION_FAILED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_IBAN_LENGTH;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.mashreq.ms.exceptions.GenericExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UAEIbanValidator {

	private static void validateChecksum(String ibanNumber) {
        /** Appending "AE" at end */
        String iban = ibanNumber.substring(2, ibanNumber.length()) + ibanNumber.substring(0, 2);
        /** Appending First two digits at end */
        iban = iban.substring(2, iban.length()) + iban.substring(0, 2);


        /** Assign the value A-10 and E-14 */
        iban = iban.substring(0, 19) + "1014" + iban.substring(21, 23);

        /** mod for four times */
        iban = Integer.toString(Integer.parseInt(iban.substring(0, 9)) % 97) + iban.substring(9, iban.length());
        iban = Integer.toString(Integer.parseInt(iban.substring(0, 9)) % 97) + iban.substring(9, iban.length());
        iban = Integer.toString(Integer.parseInt(iban.substring(0, 9)) % 97) + iban.substring(9, iban.length());
        iban = Integer.toString(Integer.parseInt(iban) % 97);

        /** If Equals "1" - valid IBAN Number */
        if (!iban.equals("1")) {
            /** Invalid IBAN Number */
            log.error("Invalid UAE IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
            GenericExceptionHandler.handleError(IBAN_CHECK_DIGIT_VALIDATION_FAILED, IBAN_CHECK_DIGIT_VALIDATION_FAILED.getErrorMessage());
        }
    }
    
    private static void validateIbanLength(String ibanNumber) {
       
    	if (null != ibanNumber && ibanNumber.length() == 23) {
    		return;
    	}
    	log.error("Invalid UAE IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
        GenericExceptionHandler.handleError(INVALID_IBAN_LENGTH, INVALID_IBAN_LENGTH.getErrorMessage());
    }
    
    public static void validateIban(String ibanNumber) {
    	log.debug("validating length of IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
    	validateIbanLength(ibanNumber);
    	
    	log.debug("validating checksum of IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
    	validateChecksum(ibanNumber);
    	
    }

}