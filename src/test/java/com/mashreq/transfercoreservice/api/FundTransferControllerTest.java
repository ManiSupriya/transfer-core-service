package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.service.TransferEligibilityProxy;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferFactory;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferServiceDefault;
import com.mashreq.transfercoreservice.fundtransfer.service.NpssEnrolmentService;
import com.mashreq.transfercoreservice.fundtransfer.service.PayLaterTransferService;
import com.mashreq.transfercoreservice.fundtransfer.service.TransferLimitService;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;
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
	private NpssEnrolmentService npssEnrolmentService;
	@Mock
	TransferLimitService transferLimitService;
	@Mock
	TwoFactorAuthRequiredCheckService twoFactorAuthRequiredCheckService;

	private FundTransferController controller;
	/** TODO: write integration test to cover contract validations */
	@Before
	public void init() {

		controller = new FundTransferController(serviceFactory,transferEligibilityProxy,
				npssEnrolmentService, transferLimitService, twoFactorAuthRequiredCheckService);
	}

	@Test(expected = GenericException.class)
	public void test_amountandSourceAmountIsNull() {
		RequestMetaData metaData = getMetaData();
		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
				new TwoFactorAuthRequiredCheckResponseDto();
		twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(false);
		//when
		when(twoFactorAuthRequiredCheckService.checkIfTwoFactorAuthenticationRequired(any(),
				any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
		controller.transferFunds(metaData , new FundTransferRequestDTO());
	}


	@Test
	public void test_withRequestWhichCanbeProcessed() {
		RequestMetaData metaData = getMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType("PL");
		request.setAmount(BigDecimal.TEN);
		request.setServiceType("WYMA");
		when(serviceFactory.getServiceAppropriateService(Mockito.eq(request))).thenReturn(payLaterTransferService);
		FundTransferResponseDTO expectedResponse = FundTransferResponseDTO.builder().build();
		when(payLaterTransferService.transferFund(metaData, request)).thenReturn(expectedResponse);
		Response transferFunds = controller.transferFunds(metaData , request);
		assertEquals(ResponseStatus.SUCCESS, transferFunds.getStatus());
		assertEquals(expectedResponse, transferFunds.getData());
	}

	@Test(expected = GenericException.class)
	public void test_withRequestWhichCannot_be_processed_due_to_otp() {
		RequestMetaData metaData = getMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType("PL");
		request.setAmount(BigDecimal.TEN);
		request.setServiceType("WAMA");

		TwoFactorAuthRequiredCheckResponseDto twoFactorAuthRequiredCheckResponseDto =
				new TwoFactorAuthRequiredCheckResponseDto();
		twoFactorAuthRequiredCheckResponseDto.setTwoFactorAuthRequired(true);
		//when
		when(twoFactorAuthRequiredCheckService.checkIfTwoFactorAuthenticationRequired(any(),
				any())).thenReturn(twoFactorAuthRequiredCheckResponseDto);
		controller.transferFunds(metaData , request);

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


	@Test
	public void testEnrolment() {
		RequestMetaData metaData = getMetaData();
		NpssEnrolmentStatusResponseDTO response = NpssEnrolmentStatusResponseDTO.builder().askForEnrolment(false).build();
		when(npssEnrolmentService.checkEnrolment(any())).thenReturn(response);
		Response enrolmentResponse = controller.retrieveNpssEnrolment(metaData);
		assertEquals(ResponseStatus.SUCCESS, enrolmentResponse.getStatus());
	}
	@Test
	public void testUpdateEnrolment() {
		RequestMetaData metaData = getMetaData();
		NpssEnrolmentUpdateResponseDTO response = NpssEnrolmentUpdateResponseDTO.builder().userEnrolmentUpdated(true).build();
		when(npssEnrolmentService.updateEnrolment(any())).thenReturn(response);
		Response enrolmentUpdateResponse = controller.updateNpssEnrolment(metaData);
		assertEquals(ResponseStatus.SUCCESS, enrolmentUpdateResponse.getStatus());
	}

	private RequestMetaData getMetaData() {
		RequestMetaData metaData = new RequestMetaData();
		return metaData;
	}

}
