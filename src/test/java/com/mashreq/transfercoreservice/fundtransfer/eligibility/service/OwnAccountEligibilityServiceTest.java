package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OwnAccountEligibilityServiceTest {

	private OwnAccountEligibilityService service;
	@Mock
	private AccountService accountService;
	@Mock
	private MaintenanceService maintenanceService;
	@Mock
	private LimitValidatorFactory limitValidatorFactory;
	@Mock
	private CurrencyValidatorFactory currencyValidatorFactory;
	@Mock
	private LimitValidator limitValidator;
	@Mock
	private CurrencyValidator currencyValidator;

	private RequestMetaData metaData = RequestMetaData.builder().build();

	@Before
	public void init() {
		service = new OwnAccountEligibilityService(
				limitValidatorFactory,
				accountService,
				maintenanceService,
				currencyValidatorFactory);
		ReflectionTestUtils.setField(service, "localCurrency", "AED");
	}



	@Test
	public void checkEligibility(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setCurrency("AED");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("AED");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibilityDiffCurrencies(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setCurrency("USD");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("AED");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}


}
