package com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators;

import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;

public interface RuleSpecificValidator {
    Validator<RuleSpecificValidatorRequest>
    getCcyValidator(String currencyCode, String transferType);
};
