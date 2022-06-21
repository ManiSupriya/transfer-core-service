package com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators;

import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleSpecificValidatorImpl implements RuleSpecificValidator {

    public final Map<String, Validator<RuleSpecificValidatorRequest>> countryValidators;

    @Override
    public Validator<RuleSpecificValidatorRequest> getCountryAndTransferTypeValidator(String countryCode, String transactionType) {
        return countryValidators.get(countryCode+"_"+transactionType+"_TransactionValidator");
    }

    @Override
    public Validator<RuleSpecificValidatorRequest> getCcyValidator(String currencyCode, String transactionType) {
        return countryValidators.get(currencyCode+"_"+transactionType+"_TransactionValidator");
    }
}
