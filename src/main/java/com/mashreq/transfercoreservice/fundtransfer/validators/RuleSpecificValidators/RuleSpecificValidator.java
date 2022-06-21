package com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators;

import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;

public interface RuleSpecificValidator {
    Validator<RuleSpecificValidatorRequest>
    getCountryAndTransferTypeValidator(String countryCode, String transferType);

    Validator<RuleSpecificValidatorRequest>
    getCcyValidator(String currencyCode, String transferType);
}
