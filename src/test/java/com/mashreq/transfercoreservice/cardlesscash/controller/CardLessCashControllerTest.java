package com.mashreq.transfercoreservice.cardlesscash.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mashreq.transfercoreservice.cache.MobRedisService;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.service.AccountService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.infrastructure.web.GlobalExceptionHandler;
import com.mashreq.webcore.dto.response.Response;

@RunWith(MockitoJUnitRunner.class)
public class CardLessCashControllerTest {

    @Mock
    CardLessCashService cardLessCashService;
    @Mock
    MobRedisService mobRedisService;

    @Mock
    AccountService accountService;

    @InjectMocks
    CardLessCashController cardLessCashController;
    
    @Mock
    CardLessCashBlockResponse cardLessCashBlockResponse;
    @Mock
    RequestMetaData metaData; 
    @Mock
    AsyncUserEventPublisher asyncUserEventPublisher;
    @Mock
    UserSessionCacheService userSessionCacheService;

    @Test
    public void blockCardLessCashRequestSuccessTest() {

        String accountNumber = "01910006432";
        String referenceNumber = "MBX246931";
        
        CardLessCashBlockRequest cardLessCashBlockRequest = CardLessCashBlockRequest.builder()
                .accountNumber(accountNumber)
                .referenceNumber(referenceNumber)
                .build();
        cardLessCashBlockResponse.setSuccess(true);
        Mockito.when(cardLessCashService.blockCardLessCashRequest(cardLessCashBlockRequest, metaData))
                .thenReturn(Response.<CardLessCashBlockResponse>builder().data(cardLessCashBlockResponse).build());
        Mockito.doReturn(true).when(userSessionCacheService).isAccountNumberBelongsToCif(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishStartedEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Response<CardLessCashBlockResponse> cashBlockResponseResponse =
                cardLessCashController.blockCardLessCashRequest(metaData, cardLessCashBlockRequest);
        Assert.assertNotNull(cashBlockResponseResponse);

    }
    
    @Test
    public void generateCardLessCashRequestSuccessTest() {

        String accountNumber = "019100064328";
        BigDecimal amount = new BigDecimal("1000");
        String mobileNo = "1910064328";
        String userId = "12345";
        CardLessCashGenerationRequest cardLessCashGenerationRequest = CardLessCashGenerationRequest.builder()
                .accountNo(accountNumber)
                .amount(amount)
                .build();
        CardLessCashGenerationResponse cardLessCashGenerationRes = new CardLessCashGenerationResponse();
        cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
        cardLessCashGenerationRes.setReferenceNumber("test");
        Mockito.when(cardLessCashService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo, userId, metaData))
                .thenReturn(Response.<CardLessCashGenerationResponse>builder().data(cardLessCashGenerationRes)
                        .build());
        when(mobRedisService.get(any(), ArgumentMatchers.<Class>any())).thenReturn("");
        when(accountService.getAccountsFromCore(any())).thenReturn(getAccountDetailsDTOS());
        Mockito.doReturn(true).when(userSessionCacheService).isAccountNumberBelongsToCif(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishStartedEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse =
                cardLessCashController.cardLessCashRemitGenerationRequest(userId, mobileNo, metaData, cardLessCashGenerationRequest);
        Assert.assertNotNull(cardLessCashGenerationResponse);

    }
    private List<AccountDetailsDTO> getAccountDetailsDTOS() {
        AccountDetailsDTO fromAcc1 = new AccountDetailsDTO();
        fromAcc1.setNumber("010797697124");

        AccountDetailsDTO fromAcc2 = new AccountDetailsDTO();
        fromAcc2.setNumber("019010050532");

        AccountDetailsDTO fromAcc3 = new AccountDetailsDTO();
        fromAcc3.setNumber("019010073901");

        return Arrays.asList(fromAcc1, fromAcc2, fromAcc3);
    }


    @Test
    public void queryCardLessCashRequestSuccessTest() {

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
		Mockito.when(cardLessCashService.cardLessCashRemitQuery(cardLessCashQueryRequest, metaData)).thenReturn(
				Response.<List<CardLessCashQueryResponse>>builder().data(cardLessCashQueryResponseList).build());
		Mockito.doReturn(true).when(userSessionCacheService).isAccountNumberBelongsToCif(Mockito.any(), Mockito.any());
		Mockito.doNothing().when(asyncUserEventPublisher).publishStartedEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(), Mockito.any());
		Response<List<CardLessCashQueryResponse>> cardLessCashQueryRes = cardLessCashController
				.cardLessCashRemitQuery(metaData, accountNumber, remitNumDays);
		Assert.assertNotNull(cardLessCashQueryRes);

	}
    
    @Test
	public void generateCardLessCashRequestMobileTest() {

		try {
			String accountNumber = "019100064328";
			BigDecimal amount = new BigDecimal("1000");
			String mobileNo = "";
			String userId = "12345";
			CardLessCashGenerationRequest cardLessCashGenerationRequest = CardLessCashGenerationRequest.builder()
					.accountNo(accountNumber).amount(amount).build();
			CardLessCashGenerationResponse cardLessCashGenerationRes = new CardLessCashGenerationResponse();
			cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
			cardLessCashGenerationRes.setReferenceNumber("test");
			Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
			Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = cardLessCashController
					.cardLessCashRemitGenerationRequest(userId, mobileNo, metaData, cardLessCashGenerationRequest);
			Assert.assertNotNull(cardLessCashGenerationResponse);
		} catch (Throwable throwable) {
			GenericException genericException = (GenericException) throwable;
			assertEquals("TN-1008", genericException.getErrorCode());
		}

	}
    
    @Test
	public void generateCardLessCashRequestAccountTest() {

		try {
			String accountNumber = "019064328";
			BigDecimal amount = new BigDecimal("1000");
			String mobileNo = "1910064328";
			String userId = "12345";
			CardLessCashGenerationRequest cardLessCashGenerationRequest = CardLessCashGenerationRequest.builder()
					.accountNo(accountNumber).amount(amount).build();
			CardLessCashGenerationResponse cardLessCashGenerationRes = new CardLessCashGenerationResponse();
			cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
			cardLessCashGenerationRes.setReferenceNumber("test");
			Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
			Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = cardLessCashController
					.cardLessCashRemitGenerationRequest(userId, mobileNo, metaData, cardLessCashGenerationRequest);
			Assert.assertNotNull(cardLessCashGenerationResponse);
		} catch (Throwable throwable) {
			GenericException genericException = (GenericException) throwable;
			assertEquals("TN-1006", genericException.getErrorCode());
		}

	}
    
}
