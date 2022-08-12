package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.service.TransferEligibilityProxy;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferFactory;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferServiceDefault;
import com.mashreq.transfercoreservice.fundtransfer.service.PayLaterTransferService;
import com.mashreq.transfercoreservice.fundtransfer.service.TransferLimitService;
import com.mashreq.transfercoreservice.util.TestUtil;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundTransferControllerTest {
	@Mock
	private FundTransferFactory serviceFactory;
	@Mock
    private TransferEligibilityProxy transferEligibilityProxy;
	@Mock
	private PayLaterTransferService payLaterTransferService;
	@Mock
	private FundTransferServiceDefault payNowService;

	@Mock
	TransferLimitService transferLimitService;

	private FundTransferController controller;
	/** TODO: write integration test to cover contract validations */
	@Before
	public void init() {
		controller = new FundTransferController(serviceFactory,transferEligibilityProxy, transferLimitService);
	}

	@Test(expected = GenericException.class)
	public void test_amountandSourceAmountIsNull() {
		RequestMetaData metaData = getMetaData();
		controller.transferFunds(metaData , new FundTransferRequestDTO());
	}


	@Test
	public void test_withRequestWhichCanbeProcessed() {
		RequestMetaData metaData = getMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType("PL");
		request.setAmount(BigDecimal.TEN);
		when(serviceFactory.getServiceAppropriateService(Mockito.eq(request))).thenReturn(payLaterTransferService);
		FundTransferResponseDTO expectedResponse = FundTransferResponseDTO.builder().build();
		when(payLaterTransferService.transferFund(metaData, request)).thenReturn(expectedResponse);
		Response transferFunds = controller.transferFunds(metaData , request);
		assertEquals(ResponseStatus.SUCCESS, transferFunds.getStatus());
		assertEquals(expectedResponse, transferFunds.getData());
	}

	@Test
	public void testEligibility() {
		RequestMetaData metaData = getMetaData();
		FundTransferEligibiltyRequestDTO request = new FundTransferEligibiltyRequestDTO();
		request.setAmount(BigDecimal.TEN);
		when(transferEligibilityProxy.checkEligibility(any(),any())).thenReturn(Collections.emptyMap());
		Response<Map<ServiceType, EligibilityResponse>> transferFunds = controller.retrieveEligibleServiceType(metaData , request);
		assertEquals(ResponseStatus.SUCCESS, transferFunds.getStatus());
		assertEquals(0, transferFunds.getData().size());
	}

	@Test
	public void should_save_transfer_details() {
		// Given
		when(transferLimitService.validateAndSaveTransferDetails(any(), any())).thenReturn(
				TransferLimitResponseDto
						.builder()
						.success(true)
						.build());
		// When
		Response<TransferLimitResponseDto> response = controller.saveTransferDetails(new RequestMetaData(),
				TestUtil.buildTransferLimitRequest(), "WQNI11082285105");

		// Then
		assertNotNull(response);
		assertTrue(response.success());
		TransferLimitResponseDto responseDto = response.getData();
		assertNotNull(responseDto);
		assertTrue(responseDto.isSuccess());
		verify(transferLimitService, times(1)).validateAndSaveTransferDetails(any(), any());
	}

	private RequestMetaData getMetaData() {
		RequestMetaData metaData = new RequestMetaData();
		return metaData;
	}

}
