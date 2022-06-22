package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static com.mashreq.transfercoreservice.util.TestUtil.getAdditionalFields;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class INFTAccountEligibilityServiceTest {

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

	private EncryptionService encryptionService = new EncryptionService();
	private RequestMetaData metaData = RequestMetaData.builder().build();

	@Before
	public void init() {
		service = new INFTAccountEligibilityService(
				accountService,
				beneficiaryValidator,
				maintenanceService,
				beneficiaryService,
				currencyValidatorFactory,
				limitValidatorFactory);
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
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(beneficiaryService.getUpdate(any(),any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}


}
