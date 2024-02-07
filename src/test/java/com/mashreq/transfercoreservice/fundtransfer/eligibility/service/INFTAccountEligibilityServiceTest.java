package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.model.DerivedEntitlements;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitManagementConfig;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_INFT_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorImpl;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.mashreq.transfercoreservice.util.TestUtil.getAdditionalFields;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class INFTAccountEligibilityServiceTest {
    @InjectMocks
    private INFTAccountEligibilityService service;
    @Mock
    private BeneficiaryService beneficiaryService;
    @Mock
    private AccountService accountService;
    @Mock
    private MaintenanceService maintenanceService;
    @Mock
    private BeneficiaryValidator beneficiaryValidator;
    @Mock
    private LimitValidatorFactory limitValidatorFactory;
    @Mock
    private CurrencyValidatorFactory currencyValidatorFactory;
    @Mock
    private CurrencyValidator currencyValidator;
    @Mock
    private LimitValidator limitValidator;
    @Mock
    private RuleSpecificValidatorImpl RuleSpecificValidatorImpl;

    @Mock
    private LimitManagementConfig limitManagementConfig;

    @Mock
    private UserSessionCacheService userSessionCacheService;

    HashMap<String, List<String>> configfields = new HashMap(){{
        put("AE", Arrays.asList("MOBILE"));
    }};

    private EncryptionService encryptionService = new EncryptionService();
    private RequestMetaData metaData = RequestMetaData.builder().build();
    private EGP_INFT_TransactionValidator egValidator;


    @BeforeEach
    public void init() {
        egValidator = new EGP_INFT_TransactionValidator();
		ReflectionTestUtils.setField(service, "localCurrency", "AED");
    }

    @Test
    public void checkEligibilityWithNoBeneUpdate() {
        FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
        fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
        fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        metaData.setCountry("AE");
        metaData.setChannel("MOBILE");

        Set<String> allowedActions = new HashSet<>();
        allowedActions.add("Inline_MoneyTransfer_Limits_EntryPoint");
        DerivedEntitlements derivedEntitlements = DerivedEntitlements.builder()
                .allowedActions(allowedActions)
                .build();

        LimitValidatorResponse expectedLimitResponse = TestUtil.limitValidatorResultsDtoEligible();

        when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
        when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(accountService.getAccountDetailsFromCache(any(), any())).thenReturn(new AccountDetailsDTO());
        when(limitManagementConfig.getCountries()).thenReturn(configfields);
        when(limitValidator.validateAvailableLimits(any(), any(), any(), any(), any())).thenReturn(expectedLimitResponse);
        when(userSessionCacheService.extractEntitlementContext(any())).thenReturn(derivedEntitlements);

        EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
        LimitValidatorResponse limitValidatorResponse = (LimitValidatorResponse) response.getData();

        assertNotNull(response);
        assertTrue(limitValidatorResponse.getIsValid());
        assertNull(limitValidatorResponse.getAmountRemark());
        assertNull(limitValidatorResponse.getCountRemark());
        assertNull(limitValidatorResponse.getNextLimitChangeDate());
        assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
    }

    @Test
    public void checkEligibilityWithNoBeneUpdateEntitlementContextIsNull() {
        FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
        fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
        fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        metaData.setCountry("AE");
        metaData.setChannel("MOBILE");

        when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
        when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
        when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(accountService.getAccountDetailsFromCache(any(), any())).thenReturn(new AccountDetailsDTO());
        when(limitManagementConfig.getCountries()).thenReturn(configfields);
        when(userSessionCacheService.extractEntitlementContext(any())).thenReturn(null);

        EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
        assertNotNull(response);
        assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
    }

    @Test
    public void checkEligibilityWithNoBeneUpdateForWEB() {
        FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
        fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
        fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        metaData.setCountry("AE");
        metaData.setChannel("WEB");
        when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
        when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
        when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(accountService.getAccountDetailsFromCache(any(), any())).thenReturn(new AccountDetailsDTO());
        when(limitManagementConfig.getCountries()).thenReturn(configfields);
        when(limitValidator.validate(any(), any(), any(), any(), any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
        EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

        assertNotNull(response);
        assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
    }

    @Test
    public void checkEligibilityWithBeneUpdate() {
        FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
        fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
        fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
        fundTransferEligibiltyRequestDTO.setBeneRequiredFields(getAdditionalFields());

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
        when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
        when(currencyValidator.validate(any(), any(), any())).thenReturn(validationResult);
        when(beneficiaryService.getUpdate(any(), any(), any(), any(), any())).thenReturn(TestUtil.getBeneficiaryDto());
        when(beneficiaryValidator.validate(any(), any(), any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(), any(), any(), any(), any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
        when(accountService.getAccountDetailsFromCache(any(), any())).thenReturn(new AccountDetailsDTO());

        EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

        assertNotNull(response);
        assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
    }

    @Test
    public void checkEligibilityFailureWithValidatorResponse() {
        FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
        fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
        fundTransferEligibiltyRequestDTO.setTxnCurrency("USD");
        fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
        fundTransferEligibiltyRequestDTO.setCurrency("EGP");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(RuleSpecificValidatorImpl.getCcyValidator(any(), any())).thenReturn(egValidator);
        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getEGBeneficiaryDto());
        when(beneficiaryValidator.validate(any(), any(), any())).thenReturn(validationResult);

        Assertions.assertThrows(GenericException.class, () -> {
            service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
        });

    }

    @Test
    public void checkEligibilityWithValidatorResponse() {
        FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
        fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
        fundTransferEligibiltyRequestDTO.setTxnCurrency("EGP");
        fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
        fundTransferEligibiltyRequestDTO.setCurrency("EGP");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
        when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);

        when(beneficiaryService.getByIdWithoutValidation(any(), any(), any(), any())).thenReturn(TestUtil.getEGBeneficiaryDto());
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(), any(), any(), any(), any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
        when(accountService.getAccountDetailsFromCache(any(), any())).thenReturn(new AccountDetailsDTO());

        EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

        assertNotNull(response);
        assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
    }
}
