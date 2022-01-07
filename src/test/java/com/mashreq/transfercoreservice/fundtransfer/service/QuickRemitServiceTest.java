package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.QuickRemitServiceClient;
import com.mashreq.transfercoreservice.client.dto.CountryDto;
import com.mashreq.transfercoreservice.client.dto.QRExchangeRequest;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.client.service.QuickRemitService;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.util.TestUtil;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.QUICK_REMIT_EXTERNAL_SERVICE_ERROR;
import static java.util.Objects.isNull;
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
