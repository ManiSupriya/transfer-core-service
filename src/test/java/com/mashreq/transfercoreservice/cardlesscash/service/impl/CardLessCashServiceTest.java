package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.common.OTPExternalConfig;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

@RunWith(MockitoJUnitRunner.class)
public class CardLessCashServiceTest {
	
	@Mock
	private AccountService accountService;
	
	@InjectMocks
	private CardLessCashServiceImpl cardLessCashServiceImpl;
	
	 @Mock
	 CardLessCashBlockResponse cardLessCashBlockResponse;
	 @Mock
	    RequestMetaData metaData; 
	 @Mock
	 AsyncUserEventPublisher asyncUserEventPublisher;
	 @Mock
	 PaymentHistoryService paymentHistoryService;
	 @Mock
	 Response<CardLessCashGenerationResponse> coreResponse;
	 @Mock
	 OTPExternalConfig otpConfig;	 
	 @Mock
	 VerifyOTPRequestDTO verifyOTPRequestDTO;	 
	 @Mock
	 OTPService iamService;
	 
    @Test
    public void blockCardLessCashRequestTest() {
    	String accountNumber = "019100064328";
        String referenceNumber = "MBX246931";
        CardLessCashBlockRequest cardLessCashBlockRequest = CardLessCashBlockRequest.builder()
                .accountNumber(accountNumber)
                .referenceNumber(referenceNumber)
                .build();
        cardLessCashBlockResponse.setSuccess(true);
        Mockito.when(accountService.blockCardLessCashRequest(cardLessCashBlockRequest))
                .thenReturn(Response.<CardLessCashBlockResponse>builder().data(cardLessCashBlockResponse).build());
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessfulEsbEvent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Response<CardLessCashBlockResponse> cashBlockResponseResponse =
        		cardLessCashServiceImpl.blockCardLessCashRequest(cardLessCashBlockRequest, metaData);
        Assert.assertNotNull(cashBlockResponseResponse);
}

	@Test
	public void cardLessCashRemitGenerationRequestTest() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		String accountNumber = "019100064328";
		BigDecimal amount = new BigDecimal("1000");
		String mobileNo = "19100064328";
		String userId = "12345";
		CardLessCashGenerationRequest cardLessCashGenerationRequest = CardLessCashGenerationRequest.builder()
				.accountNo(accountNumber).amount(amount).build();
		CardLessCashGenerationResponse cardLessCashGenerationRes = new CardLessCashGenerationResponse();
        cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
        cardLessCashGenerationRes.setReferenceNumber("test");
		Mockito.when(accountService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo))
				.thenReturn(Response.<CardLessCashGenerationResponse>builder().status(ResponseStatus.SUCCESS).errorCode("test").data(cardLessCashGenerationRes).build());
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);
		Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessfulEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
		Mockito.when(iamService.verifyOTP(Mockito.any())).thenReturn(Response.<VerifyOTPResponseDTO>builder().status(ResponseStatus.SUCCESS).data(verifyOTPResponseDTO).build());
		Mockito.doNothing().when(paymentHistoryService).insert(Mockito.any());
		Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = cardLessCashServiceImpl
				.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo, userId, metaData);
		Assert.assertNotNull(cardLessCashGenerationResponse);

	}
	@Test
	public void cardLessCashRemitQueryTest() {

        String accountNumber = "019100064328";
        Integer remitNumDays = 1;
        CardLessCashQueryRequest cardLessCashQueryRequest = CardLessCashQueryRequest.builder()
                .accountNumber(accountNumber)
                .remitNumDays(remitNumDays)
                .build();
        CardLessCashQueryResponse cardLessCashQueryResponse = new CardLessCashQueryResponse();
        cardLessCashQueryResponse.setStatus("A");
        cardLessCashQueryResponse.setAmount(new BigDecimal(1));
        cardLessCashQueryResponse.setRemitNo("1");
        cardLessCashQueryResponse.setTransactionDate(LocalDate.now());
        cardLessCashQueryResponse.setChannelName("A");
        cardLessCashQueryResponse.setRedeemedDate(LocalDate.now());
		List<CardLessCashQueryResponse> cardLessCashQueryResponseList = new ArrayList<>();
		cardLessCashQueryResponseList.add(cardLessCashQueryResponse);
		Mockito.when(accountService.cardLessCashRemitQuery(cardLessCashQueryRequest)).thenReturn(
				Response.<List<CardLessCashQueryResponse>>builder().data(cardLessCashQueryResponseList).build());
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessfulEsbEvent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Response<List<CardLessCashQueryResponse>> cardLessCashQueryRes = cardLessCashServiceImpl
				.cardLessCashRemitQuery(cardLessCashQueryRequest, metaData);
		Assert.assertNotNull(cardLessCashQueryRes);

	}

}
