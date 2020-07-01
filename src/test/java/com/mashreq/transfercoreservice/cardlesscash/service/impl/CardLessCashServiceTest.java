package com.mashreq.transfercoreservice.cardlesscash.service.impl;

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

import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.webcore.dto.response.Response;

@RunWith(MockitoJUnitRunner.class)
public class CardLessCashServiceTest {
	
	@Mock
	private AccountService accountService;
	
	@InjectMocks
	private CardLessCashServiceImpl cardLessCashServiceImpl;
	
	 @Mock
	 CardLessCashBlockResponse cardLessCashBlockResponse;
	 
    @Test
    public void blockCardLessCashRequestTest() {
    	String accountNumber = "019100064328";
        String referenceNumber = "MBX246931";
        CardLessCashBlockRequest cardLessCashBlockRequest = CardLessCashBlockRequest.builder()
                .accountNumber(accountNumber)
                .referenceNumber(referenceNumber)
                .build();

        cardLessCashBlockResponse.setSuccess(true);
        Mockito.when(cardLessCashServiceImpl.blockCardLessCashRequest(cardLessCashBlockRequest))
                .thenReturn(Response.<CardLessCashBlockResponse>builder().data(cardLessCashBlockResponse).build());
        Response<CardLessCashBlockResponse> cashBlockResponseResponse =
        		cardLessCashServiceImpl.blockCardLessCashRequest(cardLessCashBlockRequest);
        Assert.assertNotNull(cashBlockResponseResponse);
}

	@Test
	public void cardLessCashRemitGenerationRequestTest() {

        String accountNumber = "019100064328";
        BigDecimal amount = new BigDecimal("1000");
        String mobileNo = "19100064328";
        CardLessCashGenerationRequest cardLessCashGenerationRequest = CardLessCashGenerationRequest.builder()
                .accountNo(accountNumber)
                .amount(amount)
                .build();

        Mockito.when(accountService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo))
                .thenReturn(Response.<CardLessCashGenerationResponse>builder().data(
                		CardLessCashGenerationResponse.builder()
                        .expiryDateTime(LocalDateTime.now())
                        .build()
                        )
                        .build());
        Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse =
        		cardLessCashServiceImpl.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo);
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
        CardLessCashQueryResponse cardLessCashQueryResponse = CardLessCashQueryResponse.builder()
        .status("A")
        .amount(new BigDecimal(1))
        .remitNo("1")
        .transactionDate(LocalDate.now())
        .channelName("A")
        .redeemedDate(LocalDate.now())
        .build();
		List<CardLessCashQueryResponse> cardLessCashQueryResponseList = new ArrayList<>();
		cardLessCashQueryResponseList.add(cardLessCashQueryResponse);
		Mockito.when(accountService.cardLessCashRemitQuery(cardLessCashQueryRequest)).thenReturn(
				Response.<List<CardLessCashQueryResponse>>builder().data(cardLessCashQueryResponseList).build());
		Response<List<CardLessCashQueryResponse>> cardLessCashQueryRes = cardLessCashServiceImpl
				.cardLessCashRemitQuery(cardLessCashQueryRequest);
		Assert.assertNotNull(cardLessCashQueryRes);

	}

}
