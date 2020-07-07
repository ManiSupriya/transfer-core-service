package com.mashreq.transfercoreservice.fundtransfer.validators;


import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.event.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.IBAN_LENGTH_NOT_VALID;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SAME_BANK_IBAN;
import static com.mashreq.transfercoreservice.event.model.EventType.IBAN_VALIDATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class IBANValidator implements Validator {

    private static final int START_CHAR = 4;
    private static final int END_CHAR = 7;
    private final AsyncUserEventPublisher auditEventPublisher;

    //TODO take this based on country
    @Value("${app.bank.code:033}")
    private String bankCode;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {
        log.info("Validating IBAN for service type [ {} ] ", request.getServiceType());

        final int ibanLength = context.get("iban-length", Integer.class);
        if (request.getToAccount().length() != ibanLength) {
            log.info("IBAN length is invalid");
            auditEventPublisher.publishFailureEvent(IBAN_VALIDATION, metadata, null,
                    IBAN_LENGTH_NOT_VALID.getErrorMessage(),IBAN_LENGTH_NOT_VALID.getErrorMessage(), null  );
            return ValidationResult.builder().success(false).transferErrorCode(IBAN_LENGTH_NOT_VALID)
                    .build();
        }


        if (bankCode.equals(StringUtils.substring(request.getToAccount(), START_CHAR, END_CHAR))) {
            log.info("Beneficiray bank IBAN same as sender bank IBAN");
            auditEventPublisher.publishFailureEvent(IBAN_VALIDATION, metadata, null,
                    SAME_BANK_IBAN.getErrorMessage(), SAME_BANK_IBAN.getCustomErrorCode(), null);
            return ValidationResult.builder().success(false).transferErrorCode(SAME_BANK_IBAN)
                    .build();
        }

        log.info("IBAN validation successful");
        auditEventPublisher.publishSuccessEvent(IBAN_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
}
