package com.mashreq.transfercoreservice.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mashreq.mobcommons.cache.MobRedisService;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreAccountDetailsDTO;
import com.mashreq.transfercoreservice.util.TestUtil;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@ExtendWith(MockitoExtension.class)
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
	public void getAccountsIfNotInCache(){
		when(mobRedisService.get(any(),
				ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
				.thenReturn(Collections.emptyMap());
		when(accountClient.searchAccounts(any(),any()))
				.thenReturn(TestUtil.getSuccessResponse(TestUtil.getAccountDetailsDTOS()));
		accountService.getAccountsIfNotInCache(RequestMetaData.builder().userCacheKey("USSM-MOL").build());
		assertNotNull(mobRedisService.get(RequestMetaData.builder().userCacheKey("USSM-MOL").build() + "-ACCOUNTS-CONTEXT",
				new TypeReference<Map<String, Object>>() {
				}));
	}

	@Test
	public void getAccountDetailsFromCache(){
		when(userSessionCacheService.isMTAccountNumberBelongsToCif(any(), any())).thenReturn(true);
		when(accountClient.getAccountDetails(any())).thenReturn(TestUtil.getSuccessResponse(TestUtil.getCoreAccountDetails()));

		AccountDetailsDTO accountDetailsDTO = accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());

		assertNotNull(accountDetailsDTO);
		assertEquals("1234567890",accountDetailsDTO.getNumber());
	}

	@Test
	public void getAccountDetailsFromCacheError(){
		when(userSessionCacheService.isMTAccountNumberBelongsToCif(any(), any())).thenReturn(false);

		Assertions.assertThrows(GenericException.class, () -> {
			accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());
		});
	}

	@Test
	public void getAccountDetailsServiceError(){
		when(userSessionCacheService.isMTAccountNumberBelongsToCif(any(), any())).thenReturn(true);
		when(accountClient.getAccountDetails(any())).thenReturn(TestUtil.getErrorResponse(TestUtil.getCoreAccountDetails()));

		Assertions.assertThrows(GenericException.class, () -> {
			accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());
		});
	}

	@Test
	public void getAccountDetailsNoAccountError(){
		CoreAccountDetailsDTO accountDetailsDTO = TestUtil.getCoreAccountDetails();
		accountDetailsDTO.setConnectedAccounts(Collections.emptyList());
		when(userSessionCacheService.isMTAccountNumberBelongsToCif(any(), any())).thenReturn(true);
		when(accountClient.getAccountDetails(any())).thenReturn(TestUtil.getSuccessResponse(accountDetailsDTO));

		Assertions.assertThrows(GenericException.class, () -> {
			accountService.getAccountDetailsFromCache("123456789012", RequestMetaData.builder().build());
		});
	}

	@Test
	public void test_check_account_belongs_to_mashreq() {
		//Given
		String account = "123456789012";
		CoreAccountDetailsDTO coreAccountDetailsDTO = new CoreAccountDetailsDTO();
		List<SearchAccountDto> list = new ArrayList<>();
		SearchAccountDto searchAccountDto = new SearchAccountDto();
		searchAccountDto.setNumber(account);
		searchAccountDto.setAccountName("Easy saver account");
		list.add(searchAccountDto);
		coreAccountDetailsDTO.setConnectedAccounts(list);
		Response<CoreAccountDetailsDTO> response = Response.<CoreAccountDetailsDTO>builder().data(coreAccountDetailsDTO).status(ResponseStatus.SUCCESS).build();

		when(accountClient.getAccountDetails(any())).thenReturn(response);

		//Then
		Assertions.assertTrue(accountService.isAccountBelongsToMashreq(account));

	}

	@Test
	public void test_check_account_does_not_belongs_to_mashreq() {
		//Given
		String account = "123456789012";
		CoreAccountDetailsDTO coreAccountDetailsDTO = new CoreAccountDetailsDTO();
		List<SearchAccountDto> list = new ArrayList<>();
		SearchAccountDto searchAccountDto = new SearchAccountDto();
		searchAccountDto.setNumber(account);
		searchAccountDto.setAccountName("Easy saver account");
		list.add(searchAccountDto);
		coreAccountDetailsDTO.setConnectedAccounts(list);
		Response<CoreAccountDetailsDTO> response = Response.<CoreAccountDetailsDTO>builder().data(null).status(ResponseStatus.ERROR).build();

		when(accountClient.getAccountDetails(any())).thenReturn(response);

		//Then
		Assertions.assertThrows(GenericException.class, () -> accountService.isAccountBelongsToMashreq(account));

	}

}
