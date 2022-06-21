package com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators;

import com.google.common.collect.ImmutableMap;
import com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators.CountrySpecific.EG_INFT_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.RuleSpecificValidators.CurrencySpecific.EGP_WAMA_TransactionValidator;
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
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class CountrySpecificValidatorProviderImplTest {

    private RuleSpecificValidatorImpl CountrySpecificValidator;
    private EG_INFT_TransactionValidator egValidator;
    private EGP_WYMA_TransactionValidator egCcyValidator;

    private Map<String, Validator<RuleSpecificValidatorRequest>> countryValidators;
    @Before
    public void init() {
        egValidator = new EG_INFT_TransactionValidator();
        egCcyValidator = new EGP_WYMA_TransactionValidator();
        countryValidators = ImmutableMap.of("EG_INFT_TransactionValidator",egValidator,"EGP_WYMA_TransactionValidator",egCcyValidator);
        CountrySpecificValidator = new RuleSpecificValidatorImpl(countryValidators);
    }
    @Test
    public void testing_the_logic_toFind_rightValidator() {
        Validator<RuleSpecificValidatorRequest> countryValidator = CountrySpecificValidator.getCountryAndTransferTypeValidator("EG", "INFT");
        assertNotNull(countryValidator);
        assertEquals(egValidator,countryValidator);
    }
    @Test
    public void testing_the_logic_toFind_rightCcyValidator() {
        Validator<RuleSpecificValidatorRequest> countryValidator = CountrySpecificValidator.getCcyValidator("EGP", "WYMA");
        assertNotNull(countryValidator);
        assertEquals(egCcyValidator,countryValidator);
    }
    @Test
    public void testing_the_failure_logic_toFind_rightValidator() {
        Validator<RuleSpecificValidatorRequest> countryValidator = CountrySpecificValidator.getCountryAndTransferTypeValidator("EG", "QR");
        assertNull(countryValidator);
    }
}