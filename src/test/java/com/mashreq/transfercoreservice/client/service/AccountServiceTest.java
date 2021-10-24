package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.MobRedisService;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashBlockRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenReq;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashGenerationRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.request.CardLessCashQueryRequest;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashBlockResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashGenerationResponse;
import com.mashreq.transfercoreservice.cardlesscash.dto.response.CardLessCashQueryResponse;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.client.CardClient;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.common.FeesExternalConfig;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.util.TestUtil;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

	@Mock
	private AccountClient accountClient;
	@Mock
	private UserSessionCacheService userSessionCacheService;
	@Mock
	private MobRedisService mobRedisService;

	@InjectMocks
	AccountService accountService;

	@Test
	public void getAccountDetailsFromCache(){
		when(userSessionCacheService.isAccountNumberBelongsToCif(any(), any())).thenReturn(true);
		when(accountClient.getAccountDetails(any())).thenReturn(TestUtil.getSuccessResponse(TestUtil.getCoreAccountDetails()));

		AccountDetailsDTO accountDetailsDTO = accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());

		assertNotNull(accountDetailsDTO);
		assertEquals("1234567890",accountDetailsDTO.getNumber());
	}

	@Test
	public void getAccountDetailsFromCacheError(){
		when(userSessionCacheService.isAccountNumberBelongsToCif(any(), any())).thenReturn(false);

		Assertions.assertThrows(GenericException.class, () -> {
			accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());
		});
	}

	@Test
	public void getAccountDetailsServiceError(){
		when(userSessionCacheService.isAccountNumberBelongsToCif(any(), any())).thenReturn(true);
		when(accountClient.getAccountDetails(any())).thenReturn(TestUtil.getErrorResponse(TestUtil.getCoreAccountDetails()));

		Assertions.assertThrows(GenericException.class, () -> {
			accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());
		});
	}

	@Test
	public void getAccountDetailsNoAccountError(){
		CoreAccountDetailsDTO accountDetailsDTO = TestUtil.getCoreAccountDetails();
		accountDetailsDTO.setConnectedAccounts(Collections.emptyList());
		when(userSessionCacheService.isAccountNumberBelongsToCif(any(), any())).thenReturn(true);
		when(accountClient.getAccountDetails(any())).thenReturn(TestUtil.getSuccessResponse(accountDetailsDTO));

		Assertions.assertThrows(GenericException.class, () -> {
			accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());
		});
	}

}
