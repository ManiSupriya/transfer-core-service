package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.AccountNumberResolver;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.RuleSpecificValidatorImpl;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_WAMA_TransactionValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.rulespecificvalidators.currencyspecific.EGP_WYMA_TransactionValidator;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WithinAccountEligibilityServiceTest {

	private WithinAccountEligibilityService service;
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
	private LimitValidator limitValidator;
	@Mock
	private AuditEventPublisher userEventPublisher;
	
	@Mock
	private CurrencyValidatorFactory currencyValidatorFactory;
	
	@Mock
	private CurrencyValidator retailCurrencyValidator;
	
	@Mock
	private AccountNumberResolver accountNumberResolver;

	private RequestMetaData metaData = RequestMetaData.builder().build();
	@Mock
	private RuleSpecificValidatorImpl RuleSpecificValidatorProvider;
	private EGP_WAMA_TransactionValidator egWamaValidator;
	private EGP_WYMA_TransactionValidator egWymaValidator;
	@BeforeEach
	public void init() {
		egWymaValidator = new EGP_WYMA_TransactionValidator();
		egWamaValidator = new EGP_WAMA_TransactionValidator(egWymaValidator);
		service = new WithinAccountEligibilityService(
				beneficiaryValidator,
				accountService,
				beneficiaryService,
				limitValidatorFactory,
				maintenanceService,
				userEventPublisher,
				RuleSpecificValidatorProvider,
				currencyValidatorFactory,
				accountNumberResolver);
		ReflectionTestUtils.setField(service, "localCurrency", "AED");
		
		when(currencyValidatorFactory.getValidator(any())).thenReturn(retailCurrencyValidator);
		when(retailCurrencyValidator.validate(any(), any(), any())).thenReturn(ValidationResult.builder().success(true).build());
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
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
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
		
		when(accountNumberResolver.generateAccountNumber(any())).thenReturn("1234567000");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());

		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
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


		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		when(RuleSpecificValidatorProvider.getCcyValidator(any(),any())).thenReturn(egWamaValidator);
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());
		when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);

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
		fundTransferEligibiltyRequestDTO.setDestinationAccountCurrency("EGP");

		UserDTO userDTO = new UserDTO();

		ValidationResult validationResult = ValidationResult.builder().success(true).build();

		when(RuleSpecificValidatorProvider.getCcyValidator(any(),any())).thenReturn(egWamaValidator);
		when(accountService.getAccountDetailsFromCache(any(),any())).thenReturn(new AccountDetailsDTO());
		when(limitValidatorFactory.getValidator(any())).thenReturn(limitValidator);
		when(beneficiaryService.getByIdWithoutValidation(any(),any(),any(),any())).thenReturn(TestUtil.getBeneficiaryDto());
		when(beneficiaryValidator.validate(any(),any(),any())).thenReturn(validationResult);
		when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
		when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(null));


		EligibilityResponse response = service.checkEligibility(metaData, fundTransferEligibiltyRequestDTO, userDTO);

		assertNotNull(response);
		assertEquals(response.getStatus(), FundsTransferEligibility.ELIGIBLE);
	}

}
