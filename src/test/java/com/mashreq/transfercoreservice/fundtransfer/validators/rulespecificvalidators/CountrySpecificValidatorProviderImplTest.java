package com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators;

import com.google.common.collect.ImmutableMap;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_INFT_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_WYMA_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class CountrySpecificValidatorProviderImplTest {

    private RuleSpecificValidatorImpl CountrySpecificValidator;
    private EGP_INFT_TransactionValidator egValidator;
    private EGP_WYMA_TransactionValidator egCcyValidator;

    private Map<String, Validator<RuleSpecificValidatorRequest>> countryValidators;
    @BeforeEach
    public void init() {
        egValidator = new EGP_INFT_TransactionValidator();
        egCcyValidator = new EGP_WYMA_TransactionValidator();
        countryValidators = ImmutableMap.of("EGP_INFT_TransactionValidator",egValidator,"EGP_WYMA_TransactionValidator",egCcyValidator);
        CountrySpecificValidator = new RuleSpecificValidatorImpl(countryValidators);
    }
    @Test
    public void testing_the_logic_toFind_rightCcyValidator() {
        Validator<RuleSpecificValidatorRequest> countryValidator = CountrySpecificValidator.getCcyValidator("EGP", "WYMA");
        assertNotNull(countryValidator);
        assertEquals(egCcyValidator,countryValidator);
    }
}