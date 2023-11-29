package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static com.mashreq.transfercoreservice.util.TestUtil.qrExchangeResponse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.QRDealDetails;
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
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;



import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class QRAccountEligibilityServiceTest {
	@InjectMocks
	private QRAccountEligibilityService service;
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
	private QuickRemitService quickRemitService;
	@Mock
	private AuditEventPublisher userEventPublisher;
	@Mock
	private CCBalanceValidator ccBalanceValidator;
	@Mock
	private QRDealsService qrDealsService;
	@Mock
	private UserSessionCacheService userSessionCacheService;
	@Mock
	private CardService cardService;
	@Mock
	private CurrencyValidator currencyValidator;
	@Mock
	private LimitValidator limitValidator;
	@Mock
	private MobCommonService mobCommonService;
	@Mock
	private EncryptionService encryptionService;

	@Mock
	private LimitManagementConfig limitManagementConfig;

	HashMap<String, List<String>> configfields = new HashMap(){{
		put("AE", Arrays.asList("MOBILE"));
	}};
	private RequestMetaData metaData = RequestMetaData.builder().build();
	
	@Before
	public void init() {
		service = new QRAccountEligibilityService(
				beneficiaryService,
				accountService,
				maintenanceService,
				beneficiaryValidator,
				limitValidatorFactory,
				currencyValidatorFactory,
				quickRemitService,
				ccBalanceValidator,
				qrDealsService,
				userEventPublisher,
				userSessionCacheService,
				cardService,
				mobCommonService,
				limitManagementConfig);
		ReflectionTestUtils.setField(service, "countriesWhereQrDisabledForCompany", ImmutableList.of("PK"));
		ReflectionTestUtils.setField(service, "localCurrency", "AED");
	}
	
	@Test
	public void test() {
		QRExchangeResponse response = qrExchangeResponse();
		service.updateExchangeRateDisplay(response);
		assertEquals("1 USD = 114.60004 NPR",response.getExchangeRateDisplay());
		assertEquals("0.00872600",response.getExchangeRate());
		System.out.println(response.getExchangeRateDisplay());
	}

	@Test
	public void check_eligibility_if_user_is_sme(){
		//given
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setCardNo("847839687474703A2F2F7777772E74727573746564636F6D707574696E6767726F75702E6F72672F32303130");
		UserDTO userDTO = new UserDTO();
		//then
		EligibilityResponse eligibilityResponse = service.checkEligibility(RequestMetaData.builder()
				.userType("SME").build(), fundTransferEligibiltyRequestDTO, userDTO);
		assertEquals(FundsTransferEligibility.NOT_ELIGIBLE, eligibilityResponse.getStatus());
	}

	@Test
	public void throw_error_check_eligibility_for_qrcc_when_account_not_belong_to_cif(){
		//given
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setCardNo("847839687474703A2F2F7777772E74727573746564636F6D707574696E6767726F75702E6F72672F32303130");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		//when
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(userSessionCacheService.isCardNumberBelongsToCif(anyString(), anyString())).thenReturn(true);
		//then
		GenericException genericException = assertThrows(GenericException.class, () -> service.checkEligibility(RequestMetaData.builder()
						.userType("RETAIL")
						.userCacheKey("userCacheKey").build(),
				fundTransferEligibiltyRequestDTO, userDTO));
		assertEquals("TN-1006", genericException.getErrorCode());
		assertEquals("Account Number does not belong to CIF", genericException.getMessage());
	}

	@Test
	public void throw_error_check_eligibility_for_qrcc_when_qrdeal_detail_not_found(){
		//given
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setCardNo("24E6BA498AE6A32B15E0D9ACF3D99D10162C3458ABC622EF2FE5153CEA460244");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		//when
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(userSessionCacheService.isCardNumberBelongsToCif(anyString(), anyString())).thenReturn(true);
		//then
		GenericException genericException = assertThrows(GenericException.class, () -> service.checkEligibility(RequestMetaData.builder()
						.userType("RETAIL")
						.userCacheKey("userCacheKey").build(),
				fundTransferEligibiltyRequestDTO, userDTO));
		assertEquals("TN-8008", genericException.getErrorCode());
		assertEquals("Credit Card not allowed as a source of fund for this country/beneficiary. Please change the source of funds as account.", genericException.getMessage());
	}

	@Test
	public void throw_error_check_eligibility_for_qrcc_when_balance_not_sufficient(){
		//given
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setCardNo("24E6BA498AE6A32B15E0D9ACF3D99D10162C3458ABC622EF2FE5153CEA460244");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("EGP");
		fundTransferEligibiltyRequestDTO.setAmount(new BigDecimal("1000"));
		fundTransferEligibiltyRequestDTO.setDealNumber("deal1234");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();

		QRDealDetails qrDealDetails = new QRDealDetails();
		qrDealDetails.setUtilizedLimitAmount(new BigDecimal("10000"));
		qrDealDetails.setTotalLimitAmount(new BigDecimal("5000"));

		CardDetailsDTO selectedCreditCard = new CardDetailsDTO();
		CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
		currencyConversionDto.setTransactionAmount(new BigDecimal("6000"));
		//when
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(userSessionCacheService.isCardNumberBelongsToCif(anyString(), anyString())).thenReturn(true);
		when(userSessionCacheService.isCardNumberBelongsToCif(anyString(), anyString())).thenReturn(true);
		when(qrDealsService.getQRDealDetails(anyString(), anyString())).thenReturn(qrDealDetails);
		when(cardService.getCardDetailsFromCache(anyString(), any())).thenReturn(selectedCreditCard);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(currencyConversionDto);
		when(ccBalanceValidator.validate(any(), any(), any())).thenReturn(validationResult);
		//then
		GenericException genericException = assertThrows(GenericException.class, () -> service.checkEligibility(RequestMetaData.builder()
						.userType("RETAIL")
						.userCacheKey("userCacheKey")
						.primaryCif("012345678").build(),
				fundTransferEligibiltyRequestDTO, userDTO));
		assertEquals("TN-8009", genericException.getErrorCode());
		assertEquals("Credit card limit is less than the transfer amount, please enter amount with in the available limit.", genericException.getMessage());
	}

	@Test
	public void check_eligibility_for_qrcc(){
		//given
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setCardNo("24E6BA498AE6A32B15E0D9ACF3D99D10162C3458ABC622EF2FE5153CEA460244");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("EGP");
		fundTransferEligibiltyRequestDTO.setAmount(new BigDecimal("100"));
		fundTransferEligibiltyRequestDTO.setDealNumber("deal1234");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();

		QRDealDetails qrDealDetails = new QRDealDetails();
		qrDealDetails.setUtilizedLimitAmount(new BigDecimal("10000"));
		qrDealDetails.setTotalLimitAmount(new BigDecimal("50000"));

		CardDetailsDTO selectedCreditCard = new CardDetailsDTO();
		CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
		currencyConversionDto.setTransactionAmount(new BigDecimal("100"));
		//when
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(userSessionCacheService.isCardNumberBelongsToCif(anyString(), anyString())).thenReturn(true);
		when(userSessionCacheService.isCardNumberBelongsToCif(anyString(), anyString())).thenReturn(true);
		when(qrDealsService.getQRDealDetails(anyString(), anyString())).thenReturn(qrDealDetails);
		when(cardService.getCardDetailsFromCache(anyString(), any())).thenReturn(selectedCreditCard);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(currencyConversionDto);
		when(ccBalanceValidator.validate(any(), any(), any())).thenReturn(validationResult);
		//then
		EligibilityResponse response = service.checkEligibility(RequestMetaData.builder()
				.userType("RETAIL")
				.userCacheKey("userCacheKey")
				.primaryCif("012345678").build(), fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibility(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibilityMOBILE(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		metaData.setCountry("AE");
		metaData.setChannel("MOBILE");
		when(limitManagementConfig.getCountries()).thenReturn(configfields);
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validateAvailableLimits(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibilityMOBILE_DAILY_TRX_AMOUNT_ERROR(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		metaData.setCountry("AE");
		metaData.setChannel("MOBILE");
		when(limitManagementConfig.getCountries()).thenReturn(configfields);
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validateAvailableLimits(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDtoWithErrorCodes("1234"));
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.NOT_ELIGIBLE);
	}
	@Test
	public void checkEligibilityForPKBene(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(mobCommonService.getCountryValidationRules("PK")).thenReturn(TestUtil.getCountryMs());
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getPKCompanyBeneficiaryDto());

		Assertions.assertThrows(GenericException.class, () ->{
			service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
		});
	}

	@Test
	public void checkEligibilityFailure(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();
		QRExchangeResponse qrExchangeResponse = qrExchangeResponse();
		qrExchangeResponse.setAllowQR(false);

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse);

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.NOT_ELIGIBLE);
	}

	@Test
	public void checkEligibilityFailureDueToNonQRCountry(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();
		CountryDto countryDto = TestUtil.getCountryMs();
		countryDto.setQuickRemitEnabled(false);

		when(mobCommonService.getCountryValidationRules("IN")).thenReturn(countryDto);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());

		Assertions.assertThrows(GenericException.class, () ->{
				service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
		});
	}

}
