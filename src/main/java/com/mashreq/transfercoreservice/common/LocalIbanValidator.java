package com.mashreq.transfercoreservice.common;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.IBAN_CHECK_DIGIT_VALIDATION_FAILED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_IBAN_LENGTH;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.mashreq.ms.exceptions.GenericExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LocalIbanValidator {
    private final String localCountryIso;
    private final String localBankCode;
    private final int localIbanLength;
    private final int accountNumberLength;
    private final IBANValidator ibanValidator;

    private static final int BANK_CODE_INDEX = 4;

    public LocalIbanValidator(@Value("${app.local.country.iso}") String localCountryIso,
                              @Value("${app.local.bank.code}") String localBankCode,
                              @Value("${app.local.iban.length}") int localIbanLength,
                              @Value("${app.local.iban.accountNumber}") int accountNumberLength) {
        this.localCountryIso = localCountryIso;
        this.localBankCode = localBankCode;
        this.localIbanLength = localIbanLength;
        this.accountNumberLength = accountNumberLength;

        ibanValidator = IBANValidator.getInstance();
    }

    public String validate(String ibanNumber) {
        log.debug("validating length of IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
        validateIbanLength(ibanNumber);

        log.debug("validating checksum of IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
        validateChecksum(ibanNumber);

        // return the bank code
        return ibanNumber.substring(BANK_CODE_INDEX, BANK_CODE_INDEX + localBankCode.length());
    }

    public String extractAccountNumberIfMashreqIban(String iban, String bankcode) {
        if(StringUtils.isNotEmpty(iban) && localBankCode.equals(bankcode)) {
            log.info("generating account number for {}",HtmlEscapeCache.htmlEscape(iban));
            if(localIbanLength == iban.length()) {
                // returning last xx digits
                return iban.substring(localIbanLength - accountNumberLength, localIbanLength);
            }
        }
        return null;
    }

    public boolean isLocalIban(String iban) {
        return iban != null && iban.startsWith(localCountryIso);
    }

    private void validateChecksum(String ibanNumber) {
        boolean valid = ibanValidator.isValid(ibanNumber);
        if (!valid) {
            /** Invalid IBAN Number */
            log.error("Invalid local IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
            GenericExceptionHandler.handleError(IBAN_CHECK_DIGIT_VALIDATION_FAILED, IBAN_CHECK_DIGIT_VALIDATION_FAILED.getErrorMessage());
        }
    }

    private void validateChecksumOld(String ibanNumber) {

        /** Appending "AE" at end */
        String iban = ibanNumber.substring(2, ibanNumber.length()) + ibanNumber.substring(0, 2);
        /** Appending First two digits at end */
        iban = iban.substring(2) + iban.substring(0, 2);


        /** Assign the value A-10 and E-14 */
        iban = iban.substring(0, 19) + "1014" + iban.substring(21, 23);

        /** mod for four times */
        iban = Integer.toString(Integer.parseInt(iban.substring(0, 9)) % 97) + iban.substring(9);
        iban = Integer.toString(Integer.parseInt(iban.substring(0, 9)) % 97) + iban.substring(9);
        iban = Integer.toString(Integer.parseInt(iban.substring(0, 9)) % 97) + iban.substring(9);
        iban = Integer.toString(Integer.parseInt(iban) % 97);

        /** If Equals "1" - valid IBAN Number */
        if (!iban.equals("1")) {
            /** Invalid IBAN Number */
            log.error("Invalid UAE IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
            GenericExceptionHandler.handleError(IBAN_CHECK_DIGIT_VALIDATION_FAILED, IBAN_CHECK_DIGIT_VALIDATION_FAILED.getErrorMessage());
        }
    }

    
    private void validateIbanLength(String ibanNumber) {
    	if (null != ibanNumber && ibanNumber.length() == localIbanLength) {
    		return;
    	}
    	log.error("Invalid local IBAN number {}", HtmlUtils.htmlEscape(ibanNumber));
        GenericExceptionHandler.handleError(INVALID_IBAN_LENGTH, INVALID_IBAN_LENGTH.getErrorMessage());
    }

}