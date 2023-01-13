package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.MobileRedisService;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;
import java.util.Optional;

import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.*;
import static com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility.ELIGIBLE;
import static com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility.NOT_ELIGIBLE;
import static com.mashreq.transfercoreservice.util.TestUtil.getDigitalUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransferEligibilityProxyTest {

	@Mock
	private  INFTAccountEligibilityService inftAccountEligibilityService;
	@Mock
	private  LocalAccountEligibilityService localAccountEligibilityService;
	@Mock
	private  OwnAccountEligibilityService ownAccountEligibilityService;
	@Mock
	private  WithinAccountEligibilityService withinAccountEligibilityService;
	@Mock
	private  QRAccountEligibilityService qrAccountEligibilityService;
	@Mock
	private  UserSessionCacheService userSessionCacheService;
	@Mock
	private  DigitalUserRepository digitalUserRepository;
	@Mock
	private  MobCommonService mobCommonService;
	@Mock
	private MobileRedisService mobRedisService;

	@InjectMocks
	private TransferEligibilityProxy transferEligibilityProxy;

	RequestMetaData metaData = RequestMetaData.builder().build();

	@Before
	public void prepare() {
		transferEligibilityProxy.init();
	}

	@Test
	public void checkLocalEligibility(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setServiceType("LOCAL");

		when(localAccountEligibilityService.getServiceType()).thenReturn(LOCAL);
		when(localAccountEligibilityService.checkEligibility(any(),any(),any())).thenReturn(EligibilityResponse.builder().status(ELIGIBLE).build());
		when(inftAccountEligibilityService.getServiceType()).thenReturn(INFT);
		when(inftAccountEligibilityService.checkEligibility(any(),any(),any())).thenReturn(EligibilityResponse.builder().status(NOT_ELIGIBLE).build());
		when(digitalUserRepository.findByCifEquals(any())).thenReturn(Optional.<DigitalUser>of(getDigitalUser()));

		Map<ServiceType,EligibilityResponse> response = transferEligibilityProxy.checkEligibility(metaData,fundTransferEligibiltyRequestDTO);

		assertNotNull(response);
		assertTrue(response.size() == 2);
		assertEquals(ELIGIBLE, response.get(LOCAL).getStatus());
		assertEquals(NOT_ELIGIBLE, response.get(INFT).getStatus());
	}

	@Test
	public void checkWamaEligibility(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setServiceType("WAMA");

		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNoDebit(true);

		when(withinAccountEligibilityService.getServiceType()).thenReturn(WAMA);
		when(withinAccountEligibilityService.checkEligibility(any(),any(),any())).thenReturn(EligibilityResponse.builder().status(ELIGIBLE).build());
		when(digitalUserRepository.findByCifEquals(any())).thenReturn(Optional.<DigitalUser>of(getDigitalUser()));

		Map<ServiceType,EligibilityResponse> response = transferEligibilityProxy.checkEligibility(metaData,fundTransferEligibiltyRequestDTO);

		assertNotNull(response);

		assertTrue(response.size() == 1);
		assertEquals(ELIGIBLE, response.get(WAMA).getStatus());
	}

	@Test
	public void checkWamaNonEligibilityDebitFreeze(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setServiceType("WAMA");

		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNoDebit(true);

		when(withinAccountEligibilityService.getServiceType()).thenReturn(WAMA);
		when(digitalUserRepository.findByCifEquals(any())).thenReturn(Optional.<DigitalUser>of(getDigitalUser()));
		when(mobRedisService.get(any(), ArgumentMatchers.<Class>any())).thenReturn(accountDetailsDTO);

		Map<ServiceType,EligibilityResponse> response = transferEligibilityProxy.checkEligibility(metaData,fundTransferEligibiltyRequestDTO);

		assertNotNull(response);

		assertTrue(response.size() == 1);
		assertEquals(NOT_ELIGIBLE, response.get(WAMA).getStatus());
	}

	@Test
	public void checkWamaNonEligibilityCreditFreeze(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setServiceType("WAMA");

		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNoDebit(false);
		accountDetailsDTO.setNoCredit(true);

		when(withinAccountEligibilityService.getServiceType()).thenReturn(WAMA);
		when(digitalUserRepository.findByCifEquals(any())).thenReturn(Optional.<DigitalUser>of(getDigitalUser()));
		when(mobRedisService.get(any(), ArgumentMatchers.<Class>any())).thenReturn(accountDetailsDTO);

		Map<ServiceType,EligibilityResponse> response = transferEligibilityProxy.checkEligibility(metaData,fundTransferEligibiltyRequestDTO);

		assertNotNull(response);

		assertTrue(response.size() == 1);
		assertEquals(NOT_ELIGIBLE, response.get(WAMA).getStatus());
	}


	@Test
	public void checkInftEligibilityError(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");
		fundTransferEligibiltyRequestDTO.setServiceType("INFT");

		when(inftAccountEligibilityService.getServiceType()).thenReturn(INFT);
		doThrow(GenericException.class).when(inftAccountEligibilityService).checkEligibility(any(),any(),any());
		when(qrAccountEligibilityService.getServiceType()).thenReturn(QRT);
		doThrow(GenericException.class).when(qrAccountEligibilityService).checkEligibility(any(),any(),any());
		when(digitalUserRepository.findByCifEquals(any())).thenReturn(Optional.<DigitalUser>of(getDigitalUser()));
		doNothing().when(mobCommonService).checkDebitFreeze(any(),any());

		Map<ServiceType,EligibilityResponse> response = transferEligibilityProxy.checkEligibility(metaData,fundTransferEligibiltyRequestDTO);

		assertNotNull(response);
		assertTrue(response.size() == 2);
		assertEquals(NOT_ELIGIBLE, response.get(QRT).getStatus());
		assertEquals(NOT_ELIGIBLE, response.get(INFT).getStatus());
	}
}
