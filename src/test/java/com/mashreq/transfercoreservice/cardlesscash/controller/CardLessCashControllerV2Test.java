package com.mashreq.transfercoreservice.cardlesscash.controller;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequestV2;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.webcore.dto.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class CardLessCashControllerV2Test {

    @Mock
    CardLessCashService cardLessCashService;

    @Mock
    AccountService accountService;

    @InjectMocks
    CardLessCashControllerV2 cardLessCashControllerV2;
    
    @Mock
    CardLessCashBlockResponse cardLessCashBlockResponse;
    @Mock
    RequestMetaData metaData; 
    @Mock
    AsyncUserEventPublisher asyncUserEventPublisher;
    @Mock
    UserSessionCacheService userSessionCacheService;

    @Test
    public void generateCardLessCashRequestSuccessTest() {

        String accountNumber = "019100064328";
        BigDecimal amount = new BigDecimal("1000");
        String mobileNo = "1910064328";
        String userId = "12345";
        var cardLessCashGenerationRequest = CardLessCashGenerationRequest.builder()
                .accountNo(accountNumber)
                .amount(amount)
                .build();
		var cardLessCashGenerationRequestV2 = CardLessCashGenerationRequestV2.builder()
				.accountNo(accountNumber)
				.amount(amount)
				.currencyCode("AED")
				.sourceIdentifier("12312")
				.sourceType("MOB")
				.transactionType("Card Less Cash")
				.build();
        CardLessCashGenerationResponse cardLessCashGenerationRes = new CardLessCashGenerationResponse();
        cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
        cardLessCashGenerationRes.setReferenceNumber("test");
        Mockito.when(cardLessCashService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo, userId, metaData))
                .thenReturn(Response.<CardLessCashGenerationResponse>builder().data(cardLessCashGenerationRes)
                        .build());
        Mockito.doNothing().when(accountService).getAccountsIfNotInCache(any());
        Mockito.doReturn(true).when(userSessionCacheService).isAccountNumberBelongsToCif(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishStartedEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse =
                cardLessCashControllerV2.cardLessCashRemitGenerationRequest(userId, mobileNo, metaData, cardLessCashGenerationRequestV2);
        assertNotNull(cardLessCashGenerationResponse);

    }



    @Test
	public void generateCardLessCashRequestMobileTest() {

		try {
			String accountNumber = "019100064328";
			BigDecimal amount = new BigDecimal("1000");
			String mobileNo = "";
			String userId = "12345";

			var cardLessCashGenerationRequestV2 = new CardLessCashGenerationRequestV2(
					accountNumber,
					amount,
					"AED",
					"12312",
					"MOB",
					"Card Less Cash",
					"");
			var cardLessCashGenerationRes = new CardLessCashGenerationResponse();
			cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
			cardLessCashGenerationRes.setReferenceNumber("test");
			Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
			var cardLessCashGenerationResponse = cardLessCashControllerV2
					.cardLessCashRemitGenerationRequest(userId, mobileNo, metaData, cardLessCashGenerationRequestV2);
			assertNotNull(cardLessCashGenerationResponse);
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
			var cardLessCashGenerationRequestV2 = CardLessCashGenerationRequestV2.builder()
					.accountNo(accountNumber)
					.amount(amount)
					.currencyCode("AED")
					.sourceIdentifier("12312")
					.sourceType("MOB")
					.transactionType("Card Less Cash")
					.build();
			var cardLessCashGenerationRes = new CardLessCashGenerationResponse();
			cardLessCashGenerationRes.setExpiryDateTime(LocalDateTime.now());
			cardLessCashGenerationRes.setReferenceNumber("test");
			Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
			Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = cardLessCashControllerV2
					.cardLessCashRemitGenerationRequest(userId, mobileNo, metaData, cardLessCashGenerationRequestV2);
			assertNotNull(cardLessCashGenerationResponse);
		} catch (Throwable throwable) {
			GenericException genericException = (GenericException) throwable;
			assertEquals("TN-1006", genericException.getErrorCode());
		}

	}
    
}
