package com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageRepository;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.config.TwoFactorAuthRequiredValidationConfig;

@RunWith(MockitoJUnitRunner.class)
public class TwoFactorAuthRequiredCheckServiceImplTest {
	private TwoFactorAuthRequiredCheckServiceImpl service;
	@Mock
	private TwoFactorAuthRequiredValidationConfig config;
	@Mock
	private MaintenanceService maintenanceService;
	@Mock
	private BeneficiaryService beneficiaryService;
	@Mock
	private DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;
	
	private RequestMetaData metaData = RequestMetaData.builder().primaryCif("primaryCif").build();
	private String localCurrency = "AED";
	
	@Before
	public void init() {
		service = new TwoFactorAuthRequiredCheckServiceImpl(config, maintenanceService, beneficiaryService,
				digitalUserLimitUsageRepository);
		ReflectionTestUtils.setField(service, "localCurrency", localCurrency);
	}
	
	@Test
	public void test_checkIfTwoFactorAuthenticationRequired_NotRelaxedInConfig() {
		Mockito.when(config.getTwofactorAuthRelaxed()).thenReturn(false);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
				.checkIfTwoFactorAuthenticationRequired(metaData, new TwoFactorAuthRequiredCheckRequestDto());
		assertNotNull(twoFactorAuthenticationRequired);
		assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
	}
	
	@Test
	public void test_checkIfTwoFactorAuthenticationRequired_BeneficiaryRecentlyUpdated() {
		TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
		Mockito.when(config.getTwofactorAuthRelaxed()).thenReturn(true);
		Mockito.when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(true);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
				.checkIfTwoFactorAuthenticationRequired(metaData, request);
		assertNotNull(twoFactorAuthenticationRequired);
		assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
	}

	@Test
	public void test_checkIfTwoFactorAuthenticationRequired_transactionAmountExceeded() {
		TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
		Mockito.when(config.getTwofactorAuthRelaxed()).thenReturn(true);
		Mockito.when(config.getMaxAmountAllowed()).thenReturn(1);
		Mockito.when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
		Mockito.when(maintenanceService.convertToLocalCurrency(request,metaData,localCurrency)).thenReturn(BigDecimal.TEN);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
				.checkIfTwoFactorAuthenticationRequired(metaData, request);
		assertNotNull(twoFactorAuthenticationRequired);
		assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
	}

	@Test
	public void test_checkIfTwoFactorAuthenticationRequired_transactionCountExceeded() {
		TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
		request.setBeneficiaryId("222");
		Mockito.when(config.getTwofactorAuthRelaxed()).thenReturn(true);
		Mockito.when(config.getMaxAmountAllowed()).thenReturn(10);
		Mockito.when(config.getNoOfTransactionsAllowed()).thenReturn(3);
		Mockito.when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
		Mockito.when(maintenanceService.convertToLocalCurrency(request,metaData,localCurrency)).thenReturn(BigDecimal.TEN);
		Mockito.when(digitalUserLimitUsageRepository.findCountForBeneficiaryIdBetweendates(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(5l);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
				.checkIfTwoFactorAuthenticationRequired(metaData, request);
		assertNotNull(twoFactorAuthenticationRequired);
		assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
	}
	
	@Test
	public void test_checkIfTwoFactorAuthenticationRequired_ExceptionCase() {
		TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
		request.setBeneficiaryId("222");
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
				.checkIfTwoFactorAuthenticationRequired(metaData, request);
		assertNotNull(twoFactorAuthenticationRequired);
		assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
	}
	
	@Test
	public void test_checkIfTwoFactorAuthenticationRequired_NotRequiredCase() {
		TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
		request.setBeneficiaryId("222");
		Mockito.when(config.getTwofactorAuthRelaxed()).thenReturn(true);
		Mockito.when(config.getMaxAmountAllowed()).thenReturn(10);
		Mockito.when(config.getNoOfTransactionsAllowed()).thenReturn(3);
		Mockito.when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
		Mockito.when(maintenanceService.convertToLocalCurrency(request,metaData,localCurrency)).thenReturn(BigDecimal.TEN);
		Mockito.when(digitalUserLimitUsageRepository.findCountForBeneficiaryIdBetweendates(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(2l);
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
				.checkIfTwoFactorAuthenticationRequired(metaData, request);
		assertNotNull(twoFactorAuthenticationRequired);
		assertFalse(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
	}
}
