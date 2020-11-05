package com.mashreq.transfercoreservice.cardlesscash.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.mashreq.transfercoreservice.cardlesscash.service.CardLessCashService;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPRequestDTO;
import com.mashreq.transfercoreservice.client.dto.VerifyOTPResponseDTO;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.OTPService;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsage;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageRepository;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.model.Country;
import com.mashreq.transfercoreservice.model.DigitalUser;
import com.mashreq.transfercoreservice.model.DigitalUserGroup;
import com.mashreq.transfercoreservice.model.Segment;
import com.mashreq.transfercoreservice.repository.DigitalUserRepository;
import com.mashreq.transfercoreservice.transactionqueue.TransactionRepository;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

@RunWith(MockitoJUnitRunner.class)
public class CardLessCashServiceTest {
	
	@Mock
	private AccountService accountService;
	
	@InjectMocks
	private CardLessCashServiceImpl cardLessCashServiceImpl;
	
	@Mock
	private CardLessCashService cardLessCashService;
	
	 @Mock
	 CardLessCashBlockResponse cardLessCashBlockResponse;
	 @Mock
	    RequestMetaData metaData; 
	 @Mock
	 AsyncUserEventPublisher asyncUserEventPublisher;
	 @Mock
	 TransactionRepository transactionRepository;
	 @Mock
	 Response<CardLessCashGenerationResponse> coreResponse;	 
	 @Mock
	 VerifyOTPRequestDTO verifyOTPRequestDTO;	 
	 @Mock
	 OTPService iamService;
	 @Mock
	 LimitValidator limitValidator;
	 @Mock
	 DigitalUserRepository digitalUserRepository;
	 @Mock
	 BalanceValidator balanceValidator;
	 @Mock
	 DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;
	 @Mock
	 DigitalUserLimitUsage digitalUserLimitUsage;
	 @Mock
	 LimitValidatorResultsDto limitValidatorResponse;
	 @Mock
	 List<CardLessCashQueryResponse> cardLessCashQueryResponse;
	 
    @Test
    public void blockCardLessCashRequestTest() {
    	String accountNumber = "019100064328";
        String referenceNumber = "MBX246931";
        CardLessCashBlockRequest cardLessCashBlockRequest = CardLessCashBlockRequest.builder()
                .accountNumber(accountNumber)
                .referenceNumber(referenceNumber)
                .build();
        cardLessCashBlockResponse.setSuccess(true);
        Mockito.when(accountService.cardLessCashRemitQuery(Mockito.any(), Mockito.any())).thenReturn(generateCardLessCashQueryResponse());
        Mockito.when(accountService.blockCardLessCashRequest(cardLessCashBlockRequest, metaData))
                .thenReturn(Response.<CardLessCashBlockResponse>builder().data(cardLessCashBlockResponse).build());
        cardLessCashService = new CardLessCashServiceImpl(accountService, asyncUserEventPublisher, iamService, digitalUserRepository, digitalUserLimitUsageRepository, balanceValidator, limitValidator, transactionRepository);
		Response<CardLessCashBlockResponse> cashBlockResponseResponse =
				cardLessCashService.blockCardLessCashRequest(cardLessCashBlockRequest, metaData);
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
		Mockito.when(accountService.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo, metaData))
				.thenReturn(Response.<CardLessCashGenerationResponse>builder().status(ResponseStatus.SUCCESS).errorCode("test").data(cardLessCashGenerationRes).build());
		VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
		verifyOTPResponseDTO.setAuthenticated(true);
		Segment segment = new Segment();
		segment.setId(1L);
		 Country country = new Country();
		 country.setId(1L);
		 country.setLocalCurrency("AED");
		DigitalUserGroup digitalUserGroup = new DigitalUserGroup();
		digitalUserGroup.setSegment(segment);
		digitalUserGroup.setCountry(country);
		DigitalUser digitalUser = new DigitalUser();
		digitalUser.setId(2L);
		digitalUser.setCif("1234");
		digitalUser.setDigitalUserGroup(digitalUserGroup);
		Optional<DigitalUser> digiUser= Optional.of(digitalUser);
		Mockito.when(digitalUserRepository.findByCifEquals(Mockito.any())).thenReturn(digiUser);
		Mockito.when(iamService.verifyOTP(Mockito.any())).thenReturn(Response.<VerifyOTPResponseDTO>builder().status(ResponseStatus.SUCCESS).data(verifyOTPResponseDTO).build());
		Mockito.when(transactionRepository.save(Mockito.any())).thenReturn(null);
		Mockito.when(balanceValidator.validateBalance(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.when(digitalUserLimitUsageRepository.save(Mockito.any())).thenReturn(digitalUserLimitUsage);
		limitValidatorResponse.setValid(true);
		cardLessCashService = new CardLessCashServiceImpl(accountService, asyncUserEventPublisher, iamService, digitalUserRepository, digitalUserLimitUsageRepository, balanceValidator, limitValidator, transactionRepository);
		Response<CardLessCashGenerationResponse> cardLessCashGenerationResponse = cardLessCashService
				.cardLessCashRemitGenerationRequest(cardLessCashGenerationRequest, mobileNo, userId, metaData);
		Assert.assertNotNull(cardLessCashGenerationResponse);

	}
	@Test
	public void cardLessCashRemitQueryTest() {

		Mockito.when(accountService.cardLessCashRemitQuery(generateCardlessCashQueryRequest(), metaData)).thenReturn(generateCardLessCashQueryResponse());
				;
		cardLessCashService = new CardLessCashServiceImpl(accountService, asyncUserEventPublisher, iamService, digitalUserRepository, digitalUserLimitUsageRepository, balanceValidator, limitValidator, transactionRepository);
		Response<List<CardLessCashQueryResponse>> cardLessCashQueryRes = cardLessCashService
				.cardLessCashRemitQuery(generateCardlessCashQueryRequest(), metaData);
		Assert.assertNotNull(cardLessCashQueryRes);

	}
	
	CardLessCashQueryRequest generateCardlessCashQueryRequest(){
        String accountNumber = "019100064328";
        Integer remitNumDays = 1;        
		return CardLessCashQueryRequest.builder()
                .accountNumber(accountNumber)
                .remitNumDays(remitNumDays)
                .build();
	}
	Response<List<CardLessCashQueryResponse>> generateCardLessCashQueryResponse() {
        CardLessCashQueryResponse cardLessCashQueryResponse = new CardLessCashQueryResponse();
        cardLessCashQueryResponse.setStatus("A");
        cardLessCashQueryResponse.setAmount(new BigDecimal(1));
        cardLessCashQueryResponse.setRemitNo("1");
        cardLessCashQueryResponse.setTransactionDate(LocalDate.now());
        cardLessCashQueryResponse.setChannelName("A");
        cardLessCashQueryResponse.setRedeemedDate(LocalDate.now());
		List<CardLessCashQueryResponse> cardLessCashQueryResponseList = new ArrayList<>();
		cardLessCashQueryResponseList.add(cardLessCashQueryResponse);
		return Response.<List<CardLessCashQueryResponse>>builder().data(cardLessCashQueryResponseList).build();
	}

}
