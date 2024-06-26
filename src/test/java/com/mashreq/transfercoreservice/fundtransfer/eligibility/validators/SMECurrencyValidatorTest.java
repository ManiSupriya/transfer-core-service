package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;


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
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.webcore.dto.response.Response;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SMECurrencyValidatorTest {
	private SMECurrencyValidator validator;
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	@Mock
	private MobCommonClient mobCommonClient;
	private RequestMetaData metadata = RequestMetaData.builder().country("AE").build();
	
	@BeforeEach
	public void init() {
		validator = new SMECurrencyValidator(auditEventPublisher,mobCommonClient);
		ReflectionTestUtils.setField(validator, "function", "INFTALL");
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
		Response<List<CoreCurrencyDto>> response = Response.<List<CoreCurrencyDto>>builder().data(data ).build();
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency()))).thenReturn(response );
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
		Response<List<CoreCurrencyDto>> response = Response.<List<CoreCurrencyDto>>builder().data(data ).build();
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency()))).thenReturn(response );
		ValidationResult result = validator.validate(request, metadata);
		assertFalse(result.isSuccess());
		Mockito.verify(auditEventPublisher,Mockito.times(1)).publishFailureEvent(FundTransferEventType.CURRENCY_VALIDATION, metadata, null,
				TransferErrorCode.CURRENCY_IS_INVALID.getCustomErrorCode(), TransferErrorCode.CURRENCY_IS_INVALID.getErrorMessage(), null);
	}
	
	@Test
	public void test_INFT_dataEmpty() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.INFT.getName());
		request.setTxnCurrency("USD");
		Response<List<CoreCurrencyDto>> response = Response.<List<CoreCurrencyDto>>builder().data(Collections.emptyList() ).build();
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency()))).thenReturn(response );
		ValidationResult result = validator.validate(request, metadata);
		assertFalse(result.isSuccess());
	}
	
	@Test
	public void test_QRT_dataNull() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency())))
				.thenReturn(Response.<List<CoreCurrencyDto>>builder().build());
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency(request.getTxnCurrency());
		validationContext.add("country",countryMasterDto);
		ValidationResult result = validator.validate(request, metadata,validationContext );
		assertFalse(result.isSuccess());
	}
	
	@Test
	public void test_QRT_responseNull() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setServiceType(ServiceType.QRT.getName());
		request.setTxnCurrency("USD");
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency())))
				.thenReturn(null);
		ValidationContext validationContext = new ValidationContext();
		CountryMasterDto countryMasterDto = new CountryMasterDto();
		countryMasterDto.setNativeCurrency(request.getTxnCurrency());
		validationContext.add("country",countryMasterDto);
		ValidationResult result = validator.validate(request, metadata,validationContext);
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
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency())))
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
		Mockito.when(mobCommonClient.getTransferCurrencies(Mockito.any(), Mockito.eq(metadata.getCountry()),Mockito.eq(request.getTxnCurrency())))
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

}
