package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.webcore.dto.response.Response;

@ExtendWith(MockitoExtension.class)
public class CurrencyValidatorTest {
	private CurrencyValidator validator;
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	@Mock
	private MobCommonClient mobCommonClient;
	private String localCurrency = "AED";
	private RequestMetaData metadata = RequestMetaData.builder().country("AE").build();
	private String currencyFunction = "INFTALL";

	@BeforeEach
	public void init() {
		validator = new CurrencyValidator(auditEventPublisher, mobCommonClient);
		ReflectionTestUtils.setField(validator, "localCurrency", localCurrency);
		ReflectionTestUtils.setField(validator, "function", currencyFunction);
	}

	@Test
	public void test_INFT_success() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.INFT.getName());
		request.setTxnCurrency("USD");
		List<CoreCurrencyDto> data = new ArrayList<>();
		CoreCurrencyDto cur = new CoreCurrencyDto();
		cur.setCode(request.getTxnCurrency());
		cur.setSwiftTransferEnabled(true);
		cur.setQuickRemitEnabled(false);
		data.add(cur);
		Response<List<CoreCurrencyDto>> response = Response.<List<CoreCurrencyDto>>builder().data(data).build();
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(),
				Mockito.eq(metadata.getCountry()), Mockito.eq(request.getTxnCurrency()))).thenReturn(response);
		ValidationResult result = validator.validate(request, metadata);
		assertTrue(result.isSuccess());
	}

	@Test
	public void test_INFT_notEnabled() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.INFT.getName());
		request.setTxnCurrency("USD");
		List<CoreCurrencyDto> data = new ArrayList<>();
		CoreCurrencyDto cur = new CoreCurrencyDto();
		cur.setCode(request.getTxnCurrency());
		cur.setSwiftTransferEnabled(false);
		cur.setQuickRemitEnabled(false);
		data.add(cur);
		Response<List<CoreCurrencyDto>> response = Response.<List<CoreCurrencyDto>>builder().data(data).build();
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),
				Mockito.eq(request.getTxnCurrency()))).thenReturn(response);
		ValidationResult result = validator.validate(request, metadata);
		assertFalse(result.isSuccess());
		Mockito.verify(auditEventPublisher, Mockito.times(1)).publishFailureEvent(
				FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
				TransferErrorCode.CURRENCY_IS_INVALID.getCustomErrorCode(),
				TransferErrorCode.CURRENCY_IS_INVALID.getErrorMessage(), null);
	}

	@Test
	public void test_INFT_dataEmpty() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.INFT.getName());
		request.setTxnCurrency("USD");
		Response<List<CoreCurrencyDto>> response = Response.<List<CoreCurrencyDto>>builder()
				.data(Collections.emptyList()).build();
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),
				Mockito.eq(request.getTxnCurrency()))).thenReturn(response);
		ValidationResult result = validator.validate(request, metadata);
		assertFalse(result.isSuccess());
	}

	@Test
	public void test_QRT_dataNull() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),
				Mockito.eq(request.getTxnCurrency()))).thenReturn(Response.<List<CoreCurrencyDto>>builder().build());
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency(request.getTxnCurrency());
		validationContext.add("country", countryMasterDto);
		ValidationResult result = validator.validate(request, metadata, validationContext);
		assertFalse(result.isSuccess());
	}

	@Test
	public void test_QRT_responseNull() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),
				Mockito.eq(request.getTxnCurrency()))).thenReturn(null);
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency(request.getTxnCurrency());
		validationContext.add("country", countryMasterDto);
		ValidationResult result = validator.validate(request, metadata, validationContext);
		assertFalse(result.isSuccess());
	}

	@Test
	public void test_QRT_success() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		List<CoreCurrencyDto> data = new ArrayList<>();
		CoreCurrencyDto cur = new CoreCurrencyDto();
		cur.setCode(request.getTxnCurrency());
		cur.setSwiftTransferEnabled(false);
		cur.setQuickRemitEnabled(true);
		data.add(cur);
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),
				Mockito.eq(request.getTxnCurrency())))
				.thenReturn(Response.<List<CoreCurrencyDto>>builder().data(data).build());
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency(request.getTxnCurrency());
		validationContext.add("country", countryMasterDto);
		ValidationResult result = validator.validate(request, metadata, validationContext);
		assertTrue(result.isSuccess());
	}

	@Test
	public void test_QRT_notEnabled() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		List<CoreCurrencyDto> data = new ArrayList<>();
		CoreCurrencyDto cur = new CoreCurrencyDto();
		cur.setCode(request.getTxnCurrency());
		cur.setSwiftTransferEnabled(false);
		cur.setQuickRemitEnabled(false);
		data.add(cur);
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),
				Mockito.eq(request.getTxnCurrency())))
				.thenReturn(Response.<List<CoreCurrencyDto>>builder().data(data).build());
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency(request.getTxnCurrency());
		validationContext.add("country", countryMasterDto);
		ValidationResult result = validator.validate(request, metadata, validationContext);
		assertFalse(result.isSuccess());
	}

	@Test
	public void test_QRT_NativeCurrencyDoesNotMatch() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency("NIL");
		validationContext.add("country", countryMasterDto);
		ValidationResult result = validator.validate(request, metadata, validationContext);
		assertFalse(result.isSuccess());
	}
	
	@Test
	public void test_WAMA_success() {
		
		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("AED");
		
		FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setTxnCurrency("AED");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("AED");
        
        ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        mockValidationContext.add("credit-account-details", toAccount);

		ValidationResult result = validator.validate(requestDTO, metadata, mockValidationContext);
		assertTrue(result.isSuccess());
	}
	
	@Test
	public void test_WAMA_different_txn_currency() {
		
		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("AED");
		
		FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setTxnCurrency("EGP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("AED");
        
        ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        mockValidationContext.add("credit-account-details", toAccount);

		ValidationResult result = validator.validate(requestDTO, metadata, mockValidationContext);
		assertFalse(result.isSuccess());
	}
	
	@Test
	public void test_WYMA_success() {
		
		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("AED");
		ValidationContext mockValidationContext = new ValidationContext();
		
		FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
		requestDTO.setServiceType(ServiceType.WYMA.getName());
		requestDTO.setTxnCurrency("AED");
		
		AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
		toAccountDetailsDTO.setNumber("019010073766");
		toAccountDetailsDTO.setCurrency("AED");
		
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("to-account", toAccountDetailsDTO);

		ValidationResult result = validator.validate(requestDTO, metadata, mockValidationContext);
		assertTrue(result.isSuccess());
	}
	
	@Test
	public void test_WYMA_different_txn_currency() {
		
		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("AED");
		ValidationContext mockValidationContext = new ValidationContext();
		
		FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
		requestDTO.setServiceType(ServiceType.WYMA.getName());
		requestDTO.setTxnCurrency("USD");
		
		AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
		toAccountDetailsDTO.setNumber("019010073766");
		toAccountDetailsDTO.setCurrency("AED");
		
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("to-account", toAccountDetailsDTO);

		ValidationResult result = validator.validate(requestDTO, metadata, mockValidationContext);
		assertFalse(result.isSuccess());
	}
	
	@Test
	public void test_LOCAL_NonLocal_Currency() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.LOCAL.getName());
		request.setTxnCurrency("USD");
		//ValidationContext validationContext = new ValidationContext();
		ValidationResult result = validator.validate(request, metadata, null);
		assertFalse(result.isSuccess());
	}
	
	@Test
	public void test_LOCAL_success() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.LOCAL.getName());
		request.setTxnCurrency("AED");
		//ValidationContext validationContext = new ValidationContext();
		ValidationResult result = validator.validate(request, metadata, null);
		assertTrue(result.isSuccess());
	}
}
