package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.IBAN_GENERATION_FAILED;

@Component
@Slf4j
public class IbanResolver {

    private static final int ACCOUNT_NUM_LENGTH = 17;
    private static final int BANK_BRANCH_CODE_LENGTH = 4;

    public String constructIBAN(String accountNumber, String bankCode, String branchCode) {
        if (StringUtils.isBlank(accountNumber) || StringUtils.isBlank(bankCode) || StringUtils.isBlank(branchCode)) {
            log.error("[EgyptIbanResolver] Mandatory fields are null. accountNumber:{}, bankCode:{}, branchCode:{}",
                    accountNumber, bankCode, branchCode);
            GenericExceptionHandler.handleError(IBAN_GENERATION_FAILED, IBAN_GENERATION_FAILED.getErrorMessage());
        }
        try {
            String zeroPrefixedBankCode = prefixWithZeros(bankCode, BANK_BRANCH_CODE_LENGTH);
            String zeroPrefixedBranchCode = prefixWithZeros(branchCode, BANK_BRANCH_CODE_LENGTH);
            String zeroPrefixedAccountNumber = prefixWithZeros(accountNumber, ACCOUNT_NUM_LENGTH);
            Iban iban = new Iban.Builder()
                    .countryCode(CountryCode.EG)
                    .bankCode(zeroPrefixedBankCode)
                    .branchCode(zeroPrefixedBranchCode)
                    .accountNumber(zeroPrefixedAccountNumber)
                    .build();
            log.info("Generated IBAN number {}", HtmlUtils.htmlEscape(iban.toString()));
            return iban.toString();
        } catch (Exception e) {
            log.error("[IbanResolver] IBAN construction failed. accountNumber:{}, bankCode:{}, branchCode:{}",
                    accountNumber, bankCode, branchCode);
            GenericExceptionHandler.handleError(IBAN_GENERATION_FAILED, IBAN_GENERATION_FAILED.getErrorMessage());
        }
        return null;
    }

    private String prefixWithZeros(String value, int finalSize) {
        return StringUtils.leftPad(value, finalSize, "0");
    }
}
