package com.mashreq.transfercoreservice.client.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.webcore.dto.response.Response;

@ExtendWith(MockitoExtension.class)
public class MaintenanceServiceTest {
	@InjectMocks
	private MaintenanceService service;
	@Mock
	private MaintenanceClient maintenanceClient;
	private String localCurrency = "AED";
	private String primaryCif = "primaryCif";
	
	@Test
	public void test_convertToLocalCurrency_withDebitfromLocalCurrencyAccount() {
		TwoFactorAuthRequiredCheckRequestDto requestDto = new TwoFactorAuthRequiredCheckRequestDto();
		requestDto.setTxnCurrency(localCurrency);
		requestDto.setAmount(BigDecimal.TEN);
		RequestMetaData metaData = getMetaData();
		BigDecimal debitAmount = service.convertToLocalCurrency( requestDto,
			 metaData,  localCurrency);
		assertEquals(requestDto.getAmount(), debitAmount);
	}

	@Test
	public void test_convertToLocalCurrency_conversionRequired() {
		TwoFactorAuthRequiredCheckRequestDto requestDto = new TwoFactorAuthRequiredCheckRequestDto();
		requestDto.setTxnCurrency("USD");
		requestDto.setAmount(BigDecimal.TEN);
		RequestMetaData metaData = getMetaData();
		CurrencyConversionDto response = new CurrencyConversionDto();
		response.setAccountCurrencyAmount(BigDecimal.TEN);
		Mockito.when(maintenanceClient.convertBetweenCurrencies(Mockito.any())).thenReturn(Response.<CurrencyConversionDto>builder().data(response).build());
		BigDecimal debitAmount = service.convertToLocalCurrency( requestDto,
			 metaData,  localCurrency);
		assertEquals(response.getAccountCurrencyAmount(), debitAmount);
	}

	@Test
	public void test_convertToLocalCurrency_conversionRequired_withDeal() {
		TwoFactorAuthRequiredCheckRequestDto requestDto = new TwoFactorAuthRequiredCheckRequestDto();
		requestDto.setTxnCurrency("USD");
		requestDto.setAccountCurrency(localCurrency);
		requestDto.setAmount(BigDecimal.TEN);
		requestDto.setDealNumber("deal123");
		RequestMetaData metaData = getMetaData();
		CurrencyConversionDto response = new CurrencyConversionDto();
		response.setAccountCurrencyAmount(BigDecimal.TEN);
		Mockito.when(maintenanceClient.convertBetweenCurrencies(Mockito.any())).thenReturn(Response.<CurrencyConversionDto>builder().data(response).build());
		ArgumentCaptor<CoreCurrencyConversionRequestDto> param =  ArgumentCaptor.forClass(CoreCurrencyConversionRequestDto.class);
		BigDecimal debitAmount = service.convertToLocalCurrency( requestDto,
			 metaData,  localCurrency);
		Mockito.verify(maintenanceClient,Mockito.times(1)).convertBetweenCurrencies(param.capture());
		assertEquals(response.getAccountCurrencyAmount(), debitAmount);
		assertNotNull(param.getValue());
		assertEquals("deal123", param.getValue().getDealNumber());
	}
	
	@Test
	public void test_convertToLocalCurrency_conversionRequired_withInvalidDeal() {
		TwoFactorAuthRequiredCheckRequestDto requestDto = new TwoFactorAuthRequiredCheckRequestDto();
		requestDto.setTxnCurrency("USD");
		requestDto.setAccountCurrency("EUR");
		requestDto.setAmount(BigDecimal.TEN);
		requestDto.setDealNumber("deal123");
		RequestMetaData metaData = getMetaData();
		CurrencyConversionDto response = new CurrencyConversionDto();
		response.setAccountCurrencyAmount(BigDecimal.TEN);
		Mockito.when(maintenanceClient.convertBetweenCurrencies(Mockito.any())).thenReturn(Response.<CurrencyConversionDto>builder().data(response).build());
		ArgumentCaptor<CoreCurrencyConversionRequestDto> param =  ArgumentCaptor.forClass(CoreCurrencyConversionRequestDto.class);
		BigDecimal debitAmount = service.convertToLocalCurrency( requestDto,
			 metaData,  localCurrency);
		Mockito.verify(maintenanceClient,Mockito.times(1)).convertBetweenCurrencies(param.capture());
		assertEquals(response.getAccountCurrencyAmount(), debitAmount);
		assertNotNull(param.getValue());
		assertNull(param.getValue().getDealNumber());
	}
	
	private RequestMetaData getMetaData() {
		return RequestMetaData.builder().primaryCif(primaryCif).build();
	}
}
