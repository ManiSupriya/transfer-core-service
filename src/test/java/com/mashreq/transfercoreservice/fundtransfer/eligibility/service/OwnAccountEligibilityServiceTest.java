package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
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
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_WYMA_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorImpl;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.util.TestUtil;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
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
	@Mock
	private RuleSpecificValidatorImpl RuleSpecificValidatorProvider;

	private RequestMetaData metaData = RequestMetaData.builder().build();
	private EGP_WYMA_TransactionValidator egWymaValidator;

	@Before
	public void init() {
		egWymaValidator = new EGP_WYMA_TransactionValidator();
		service = new OwnAccountEligibilityService(
				limitValidatorFactory,
				accountService,
				maintenanceService,
				currencyValidatorFactory,
				RuleSpecificValidatorProvider);
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
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
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
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

	@Test
	public void checkEligibilityFailureWithValidatorResponse(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("USD");
		fundTransferEligibiltyRequestDTO.setFromAccount("ASDFGH");
		fundTransferEligibiltyRequestDTO.setToAccount("QWERTY");
		fundTransferEligibiltyRequestDTO.setCurrency("EGP");
		fundTransferEligibiltyRequestDTO.setDestinationAccountCurrency("USD");

		UserDTO userDTO = new UserDTO();
		AccountDetailsDTO toAccount = new AccountDetailsDTO();
		toAccount.setCurrency("USD");
		AccountDetailsDTO fromAccount = new AccountDetailsDTO();
		fromAccount.setCurrency("EGP");


		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(RuleSpecificValidatorProvider.getCcyValidator(any(),any())).thenReturn(egWymaValidator);
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
		when(accountService.getAccountDetailsFromCache(eq("ASDFGH"),any())).thenReturn(fromAccount);
		when(accountService.getAccountDetailsFromCache(eq("QWERTY"),any())).thenReturn(toAccount);

		Assertions.assertThrows(GenericException.class, () ->{
			service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
		});

	}
	@Test
	public void checkEligibilityFailureCase2WithValidatorResponse(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("USD");
		fundTransferEligibiltyRequestDTO.setFromAccount("ASDFGH");
		fundTransferEligibiltyRequestDTO.setToAccount("QWERTY");
		fundTransferEligibiltyRequestDTO.setCurrency("EGP");
		fundTransferEligibiltyRequestDTO.setDestinationAccountCurrency("USD");

		UserDTO userDTO = new UserDTO();
		AccountDetailsDTO toAccount = new AccountDetailsDTO();
		toAccount.setCurrency("EGP");
		AccountDetailsDTO fromAccount = new AccountDetailsDTO();
		fromAccount.setCurrency("EGP");


		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(RuleSpecificValidatorProvider.getCcyValidator(any(),any())).thenReturn(egWymaValidator);
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
		when(accountService.getAccountDetailsFromCache(eq("ASDFGH"),any())).thenReturn(fromAccount);
		when(accountService.getAccountDetailsFromCache(eq("QWERTY"),any())).thenReturn(toAccount);

		Assertions.assertThrows(GenericException.class, () ->{
			service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);
		});

	}

	@Test
	public void checkEligibilityWithValidatorResponse(){
		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setTxnCurrency("EGP");
		fundTransferEligibiltyRequestDTO.setFromAccount("ASDFGH");
		fundTransferEligibiltyRequestDTO.setToAccount("QWERTY");
		fundTransferEligibiltyRequestDTO.setCurrency("EGP");
		fundTransferEligibiltyRequestDTO.setDestinationAccountCurrency("USD");

		UserDTO userDTO = new UserDTO();
		AccountDetailsDTO toAccount = new AccountDetailsDTO();
		toAccount.setCurrency("EGP");
		AccountDetailsDTO fromAccount = new AccountDetailsDTO();
		fromAccount.setCurrency("EGP");

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(RuleSpecificValidatorProvider.getCcyValidator(any(),any())).thenReturn(egWymaValidator);
		when(currencyValidatorFactory.getValidator(any())).thenReturn(currencyValidator);
		when(currencyValidator.validate(any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(eq("ASDFGH"),any())).thenReturn(fromAccount);
		when(accountService.getAccountDetailsFromCache(eq("QWERTY"),any())).thenReturn(toAccount);


		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

}
