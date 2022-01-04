package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static com.mashreq.transfercoreservice.util.TestUtil.getAdditionalFields;
import static com.mashreq.transfercoreservice.util.TestUtil.qrExchangeResponse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;

@RunWith(MockitoJUnitRunner.class)
public class QRAccountEligibilityServiceTest {

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

	private EncryptionService encryptionService = new EncryptionService();
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
				mobCommonService);
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
	public void checkEligibility(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(maintenanceService.getAllCountries(any(),any(),any())).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto());
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse());
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
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
		when(maintenanceService.getAllCountries(any(),any(),any())).thenReturn(TestUtil.getCountryMs());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(quickRemitService.exchange(any(),any(),any())).thenReturn(qrExchangeResponse);

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.NOT_ELIGIBLE);
	}

}
