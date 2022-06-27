package com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators.CurrencySpecific;

        import com.mashreq.mobcommons.services.http.RequestMetaData;
        import com.mashreq.transfercoreservice.errors.TransferErrorCode;
        import com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators.RuleSpecificValidatorRequest;
        import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
        import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
        import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
        import lombok.extern.slf4j.Slf4j;
        import org.apache.commons.lang3.StringUtils;
        import org.springframework.stereotype.Component;

        import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
        import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CREDIT_NOT_ALLOWED_FOR_EGP;
        import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DEBIT_NOT_ALLOWED_FOR_EGP;


@Slf4j
@Component("EGP_WYMA_TransactionValidator")
public class EGP_WYMA_TransactionValidator implements Validator<RuleSpecificValidatorRequest> {

    private String CcyCode = "EGP";

    @Override
    public ValidationResult validate(
            RuleSpecificValidatorRequest request,
            RequestMetaData metadata,
            ValidationContext context) {

        final boolean isDestinationAccountEGP = StringUtils.isNotBlank(request.getDestinationAccountCurrency()).equalsIgnoreCase(CcyCode);
        final boolean isSourceAccountEGP = request.getSourceAccountCurrency().equalsIgnoreCase(CcyCode);
        final boolean debitAccountIssue = isDestinationAccountEGP && !isSourceAccountEGP;
        final boolean creditAccountIssue = StringUtils.isNotBlank(request.getDestinationAccountCurrency()) &&
                !isDestinationAccountEGP && isSourceAccountEGP;
        final boolean txnCcyIssue = (isDestinationAccountEGP || isSourceAccountEGP) &&
                !request.getTxnCurrency().equalsIgnoreCase(CcyCode);

        if (debitAccountIssue) {
            log.info("Validating SourceAccountCurrency is not EGP ");
            return prepareValidationResult(Boolean.FALSE, DEBIT_NOT_ALLOWED_FOR_EGP);
        }else if(creditAccountIssue){
            log.info("Validating DestinationAccountCurrency is not EGP ");
            return prepareValidationResult(Boolean.FALSE, CREDIT_NOT_ALLOWED_FOR_EGP);
        }else if(txnCcyIssue){
            log.info("Validating transactionCurrency is not EGP ");
            return prepareValidationResult(Boolean.FALSE, ACCOUNT_CURRENCY_MISMATCH);
        }
        log.info("Validating EGP - EGP success ");
        return prepareValidationResult(Boolean.TRUE, null);
    };

    private ValidationResult prepareValidationResult(boolean status, TransferErrorCode errorCode) {
        return status ? ValidationResult.builder().success(true).build() : ValidationResult.builder()
                .success(false)
                .transferErrorCode(errorCode)
                .build();
    };
}
