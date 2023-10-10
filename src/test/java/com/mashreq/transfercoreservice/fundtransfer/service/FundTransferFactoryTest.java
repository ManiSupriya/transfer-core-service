package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.microsoft.sqlserver.jdbc.StringUtils;

@ExtendWith(MockitoExtension.class)
public class FundTransferFactoryTest {
	@Mock
	private PayLaterTransferService payLaterTransferService;
	@Mock
	private FundTransferServiceDefault payNowService;
	
	private FundTransferFactory factory;
	
	@BeforeEach
	public void init() {
		factory = new FundTransferFactory(payLaterTransferService,payNowService);
	}
	
	@Test
	public void test_ordertypeisempty_tokeepBackwardCompatibility() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType(StringUtils.EMPTY);
		FundTransferService service = factory.getServiceAppropriateService(request);
		assertEquals(payNowService, service);
	}

	@Test
	public void test_ordertypeispaynow() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType("PN");
		FundTransferService service = factory.getServiceAppropriateService(request);
		assertEquals(payNowService, service);
	}
	
	@Test
	public void test_ordertypeispaylater() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType("PL");
		FundTransferService service = factory.getServiceAppropriateService(request);
		assertEquals(payLaterTransferService, service);
	}
	
	@Test
	public void test_ordertypeisStandingInstruction() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		request.setOrderType("SI");
		FundTransferService service = factory.getServiceAppropriateService(request);
		assertEquals(payLaterTransferService, service);
	}
}
