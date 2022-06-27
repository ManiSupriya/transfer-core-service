package com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators;

import com.google.common.collect.ImmutableMap;
import com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators.CurrencySpecific.EGP_INFT_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators.CurrencySpecific.EGP_WYMA_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class CountrySpecificValidatorProviderImplTest {

    private RuleSpecificValidatorImpl CountrySpecificValidator;
    private EGP_INFT_TransactionValidator egValidator;
    private EGP_WYMA_TransactionValidator egCcyValidator;

    private Map<String, Validator<RuleSpecificValidatorRequest>> countryValidators;
    @Before
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