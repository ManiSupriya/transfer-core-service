package com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorRequest;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CREDIT_NOT_ALLOWED_FOR_EGP;

@Slf4j
@Component("EGP_INFT_TransactionValidator")
public class EGP_INFT_TransactionValidator implements Validator<RuleSpecificValidatorRequest> {

    @Override
    public ValidationResult validate(
            RuleSpecificValidatorRequest request,
            RequestMetaData metadata,
            ValidationContext context) {
        final String currencyCode = "EGP";
        final boolean debitAccountIssue = !request.getTxnCurrency().equalsIgnoreCase(currencyCode) &&
                request.getSourceAccountCurrency().equalsIgnoreCase(currencyCode);

        if (debitAccountIssue) {
            log.info("Validating SourceAccountCurrency is not EGP ");
            return prepareValidationResult(Boolean.FALSE, CREDIT_NOT_ALLOWED_FOR_EGP);
        }
        log.info("Validating EGP - EGP success ");
        return prepareValidationResult(Boolean.TRUE, null);
    }

    private ValidationResult prepareValidationResult(boolean status, TransferErrorCode errorCode) {
        return status ? ValidationResult.builder().success(true).build() : ValidationResult.builder()
                .success(false)
                .transferErrorCode(errorCode)
                .build();
    }
}
