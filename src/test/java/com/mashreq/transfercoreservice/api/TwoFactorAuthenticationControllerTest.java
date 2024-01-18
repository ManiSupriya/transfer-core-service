package com.mashreq.transfercoreservice.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

@ExtendWith(MockitoExtension.class)
public class TwoFactorAuthenticationControllerTest {
	private TwoFactorAuthenticationController controller;
	@Mock
	private TwoFactorAuthRequiredCheckService service;
	
	@BeforeEach
	public void init() {
		controller = new TwoFactorAuthenticationController(service);
	}
	
	@Test
	public void test_checkIfTwoFactorAuthenticationRequired() {
		TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
		RequestMetaData metadata = RequestMetaData.builder().build();
		TwoFactorAuthRequiredCheckResponseDto data = new TwoFactorAuthRequiredCheckResponseDto();
		Mockito.when(service.checkIfTwoFactorAuthenticationRequired(metadata, request)).thenReturn(data);
		Response response = controller.checkIfTwoFactorAuthenticationRequired(metadata,  request);
		assertEquals(ResponseStatus.SUCCESS, response.getStatus());
		assertEquals(data, response.getData());
	}

}
