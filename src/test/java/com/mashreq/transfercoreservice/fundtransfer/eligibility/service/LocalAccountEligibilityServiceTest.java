package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitManagementConfig;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.test.util.ReflectionTestUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_LOCAL_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.mashreq.transfercoreservice.util.TestUtil.getAdditionalFields;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalAccountEligibilityServiceTest {

	@InjectMocks
	private LocalAccountEligibilityService service;
	@Mock
	private BeneficiaryService beneficiaryService;
	@Mock
	private AccountService accountService;
	@Mock
	private CardService cardService;
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
	private UserSessionCacheService userSessionCacheService;
	@Mock
	private AuditEventPublisher auditEventPublisher;
	@Mock
	private CCBalanceValidator ccBalanceValidator;
	@Mock
	private QRDealsService qrDealsService;
	@Mock
	private RuleSpecificValidatorImpl RuleSpecificValidatorImpl;

	@Mock
	private LimitManagementConfig limitManagementConfig;

	private EncryptionService encryptionService = new EncryptionService();
	private RequestMetaData metaData = RequestMetaData.builder().build();
	private EGP_LOCAL_TransactionValidator egValidator;

	HashMap<String, List<String>> configfields = new HashMap(){{
		put("AE", Arrays.asList("MOBILE"));
	}};


	@Before
	public void setUp() {

		ReflectionTestUtils.setField(service, "localCurrency", "AED");
	}
	@Test
	public void checkEligibilityWithNoBeneUpdate(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibilityWithBeneUpdate(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setBeneRequiredFields(getAdditionalFields());

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		metaData.setCountry("AE");
		metaData.setChannel("MOBILE");
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitManagementConfig.getCountries()).thenReturn(configfields);
		when(limitValidator.validateAvailableLimits(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibilityWithBeneUpdateWEB(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setBeneRequiredFields(getAdditionalFields());

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		metaData.setCountry("AE");
		metaData.setChannel("WEB");
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitManagementConfig.getCountries()).thenReturn(configfields);
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkCCEligibilityWithNoBeneUpdate(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setAmount(BigDecimal.ZERO);
		fundTransferEligibiltyRequestDTO.setCardNo("7E0CF3390CFFE58BEE3D84583A435FC01F83E86CEB8B10A849AC63CA43E7D924");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		metaData.setCountry("AE");
		metaData.setChannel("MOBILE");
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(limitManagementConfig.getCountries()).thenReturn(configfields);
		when(limitValidator.validateAvailableLimits(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(cardService.getCardDetailsFromCache(any(),any())).thenReturn(new CardDetailsDTO());
		when(userSessionCacheService.isCardNumberBelongsToCif(any(),any())).thenReturn(true);
		when(ccBalanceValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(qrDealsService.getQRDealDetails(any(),any())).thenReturn(TestUtil.getQRDealsDetails());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkCCEligibilityWithNoBeneUpdateWEB(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setAmount(BigDecimal.ZERO);
		fundTransferEligibiltyRequestDTO.setCardNo("7E0CF3390CFFE58BEE3D84583A435FC01F83E86CEB8B10A849AC63CA43E7D924");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		metaData.setCountry("AE");
		metaData.setChannel("WEB");
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(limitManagementConfig.getCountries()).thenReturn(configfields);
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(cardService.getCardDetailsFromCache(any(),any())).thenReturn(new CardDetailsDTO());
		when(userSessionCacheService.isCardNumberBelongsToCif(any(),any())).thenReturn(true);
		when(ccBalanceValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(qrDealsService.getQRDealDetails(any(),any())).thenReturn(TestUtil.getQRDealsDetails());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}


}
