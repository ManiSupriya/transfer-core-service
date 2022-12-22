package com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorRequest;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Profile("!egypt")
@Slf4j
@RequiredArgsConstructor
@Component("EGP_WAMA_TransactionValidator")
public class EGP_WAMA_TransactionValidator implements Validator<RuleSpecificValidatorRequest> {
    private final EGP_WYMA_TransactionValidator validator;

    @Override
    public ValidationResult validate(
            RuleSpecificValidatorRequest request,
            RequestMetaData metadata,
            ValidationContext context) {

        return validator.validate(request, metadata, context);
    }
}
