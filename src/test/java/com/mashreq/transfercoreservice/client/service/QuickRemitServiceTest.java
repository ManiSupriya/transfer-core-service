package com.mashreq.transfercoreservice.client.service;


import java.util.Optional;

import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.QuickRemitServiceClient;
import com.mashreq.transfercoreservice.client.dto.CountryDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.QRExchangeRequest;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class QuickRemitServiceTest {

	private QuickRemitService service;
	@Mock
	private QuickRemitServiceClient quickRemitServiceClient;
	@Mock
	private AsyncUserEventPublisher userEventPublisher;
	
	@BeforeEach
	public void init() {
		service = new QuickRemitService(quickRemitServiceClient, userEventPublisher);
	}
	
	@Test
	public void test_DataPassing_toExternal_api() {
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setTxnCurrency("txnCurrency");
		request.setBeneficiaryId("123");
		RequestMetaData metaData = RequestMetaData.builder().build();
		ArgumentCaptor<QRExchangeRequest> captor = ArgumentCaptor.forClass(QRExchangeRequest.class);
		Response<QRExchangeResponse> response = Response.<QRExchangeResponse>builder().status(ResponseStatus.SUCCESS).data(new QRExchangeResponse()).build();
		Mockito.when(quickRemitServiceClient.exchange(Mockito.any())).thenReturn(response );
		service.exchange(request, new CountryDto(), metaData);
		Mockito.verify(quickRemitServiceClient,Mockito.times(1)).exchange(captor.capture());
		QRExchangeRequest req = captor.getValue();
		assertEquals("txnCurrency", req.getDestinationCcy());
	}

}
