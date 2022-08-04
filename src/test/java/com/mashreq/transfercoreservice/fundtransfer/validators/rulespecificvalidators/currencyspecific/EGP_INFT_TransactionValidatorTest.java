package com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific;

import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class EGP_INFT_TransactionValidatorTest {
    private EGP_INFT_TransactionValidator egValidator;
    @Before
    public void init() {
        egValidator = new EGP_INFT_TransactionValidator();
    }
    @Test
    public void testing_the_FAIL_logic_INFT_Validator() {
        RuleSpecificValidatorRequest validationRequest =
                RuleSpecificValidatorRequest.builder()
                        .sourceAccountCurrency("EGP")
                        .txnCurrency("USD").build();

        ValidationResult result = egValidator.validate(validationRequest, null, null);
        assertNotNull(result);
        assertEquals(result.isSuccess(),false);
    }
    @Test
    public void testing_the_SUCCESS_logic_INFT_Validator() {
        RuleSpecificValidatorRequest validationRequest =
                RuleSpecificValidatorRequest.builder()
                        .sourceAccountCurrency("EGP")
                        .txnCurrency("EGP").build();

        ValidationResult result = egValidator.validate(validationRequest, null, null);
        assertNotNull(result);
        assertEquals(result.isSuccess(),true);
    }

}