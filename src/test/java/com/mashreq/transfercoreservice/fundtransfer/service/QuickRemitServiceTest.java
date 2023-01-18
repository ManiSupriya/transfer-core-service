package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.QuickRemitServiceClient;
import com.mashreq.transfercoreservice.client.dto.CountryDto;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.client.service.QuickRemitService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitServiceTest {

	@Mock
	private QuickRemitServiceClient quickRemitServiceClient;

	@InjectMocks
	private QuickRemitService quickRemitService;

	@Test
	public void exchangeTest(){

		FundTransferEligibiltyRequestDTO fundTransferEligibiltyRequestDTO = new FundTransferEligibiltyRequestDTO();
		fundTransferEligibiltyRequestDTO.setBeneficiaryId("1");
		fundTransferEligibiltyRequestDTO.setFromAccount("1234567890");

		when(quickRemitServiceClient.exchange(any())).thenReturn(TestUtil.getSuccessResponse(new QRExchangeResponse()));

		Assertions.assertNotNull(quickRemitService.exchange(fundTransferEligibiltyRequestDTO, new CountryDto(), RequestMetaData.builder().build()));
	}
}
