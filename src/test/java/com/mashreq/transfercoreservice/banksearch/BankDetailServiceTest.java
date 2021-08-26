package com.mashreq.transfercoreservice.banksearch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

import java.util.Collections;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.OmwCoreClient;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.MashreqUAEAccountNumberResolver;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.repository.BankRepository;

@RunWith(MockitoJUnitRunner.class)
public class BankDetailServiceTest {

	private static final String MASHREQ_SWIFT = "BOMLEADX";
	private BankDetailService service;
	@Mock
	private IbanSearchMWService ibanSearchMWService;
	@Mock
    private RoutingCodeSearchMWService routingCodeSearchMWService;
	@Mock
    private IfscCodeSearchMWService ifscCodeSearchMWService;
	@Mock
    private OmwCoreClient omwClient;
	@Mock
    private SwiftBankDetailsMapper bankDetailsMapper;
	@Mock
    private SoapServiceProperties soapServiceProperties;
	@Mock
    private BICCodeSearchService bicCodeSearchService;
	@Mock
    private BankRepository bankRepository;
	@Mock
    private MobCommonService mobCommonService;
	@Mock
    private MashreqUAEAccountNumberResolver accountNumberResolver;
	
	@Before
	public void init() {
		service = new BankDetailService(ibanSearchMWService, routingCodeSearchMWService, ifscCodeSearchMWService,
				omwClient, bankDetailsMapper, soapServiceProperties, bicCodeSearchService, bankRepository,
				accountNumberResolver, mobCommonService);
	}
	
	
	@Test
	public void test_accountNumber() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("AE");
		request.setType("iban");
		request.setValue("AE280330000010698008304");
		request.setJourneyType("MT");
		RequestMetaData metadata = new RequestMetaData();
		metadata.setChannelTraceId("whrvh3b4h5bh6");
		BankDetails bankDetails = new BankDetails();
		bankDetails.setBankCode("033");
		bankDetails.setBankName("Mashreq Bank PSC");
		bankDetails.setSwiftCode("BOMLEADX");
		String accNo = "010698008304";
		Mockito.when(bankRepository.findByBankCode("033")).thenReturn(Optional.of(bankDetails));
		Mockito.when(accountNumberResolver.generateAccountNumber(Mockito.anyString())).thenReturn(accNo);
		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		assertEquals(1, response.size());
		assertEquals(accNo, response.get(0).getAccountNo());
	}

	@Test
	public void test_swiftCodeUpdateifSwiftCodeIs8Digit() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("AE");
		request.setType("swift");
		request.setValue(MASHREQ_SWIFT);
		request.setJourneyType("MT");
		RequestMetaData metadata = new RequestMetaData();
		metadata.setChannelTraceId("whrvh3b4h5bh6");
		/*
		 * BankDetails bankDetails = new BankDetails(); bankDetails.setBankCode("033");
		 * bankDetails.setBankName("Mashreq Bank PSC");
		 * bankDetails.setSwiftCode("BOMLEADXXX");
		 */
		String accNo = "010698008304";
		ArgumentCaptor<String> updatedSwift = ArgumentCaptor.forClass(String.class);
		BankResultsDto dto =new BankResultsDto();
		dto.setCountryCode("AE");
		dto.setAccountNo(accNo);
		dto.setSwiftCode(MASHREQ_SWIFT);
		dto.setBankCountry("United Arab Emirates");
		List<BankResultsDto> resultList = Arrays.asList(dto);
		Mockito.when(routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(resultList);
		Map<String, String> countryMap = new HashedMap<>();
		countryMap.put("AE", "United Arab Emirates");
		//Mockito.when(mobCommonService.getCountryCodeMap()).thenReturn(countryMap);
		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		Mockito.verify(bankRepository,times(1)).getBankCode(Mockito.anyString(), Mockito.eq(MASHREQ_SWIFT), updatedSwift.capture());
		assertEquals(1, response.size());
		assertEquals(accNo, response.get(0).getAccountNo());
		assertEquals(MASHREQ_SWIFT+"XXX", updatedSwift.getValue());
	}
	
	@Test
	public void test_swiftCodeUpdateifSwiftCodeIs11Digit() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("AE");
		request.setType("swift");
		request.setValue(MASHREQ_SWIFT);
		request.setJourneyType("MT");
		RequestMetaData metadata = new RequestMetaData();
		metadata.setChannelTraceId("whrvh3b4h5bh6");
		/*
		 * BankDetails bankDetails = new BankDetails(); bankDetails.setBankCode("033");
		 * bankDetails.setBankName("Mashreq Bank PSC");
		 * bankDetails.setSwiftCode("BOMLEADXXX");
		 */
		String accNo = "010698008304";
		ArgumentCaptor<String> updatedSwift = ArgumentCaptor.forClass(String.class);
		BankResultsDto dto =new BankResultsDto();
		dto.setCountryCode("AE");
		dto.setAccountNo(accNo);
		dto.setSwiftCode(MASHREQ_SWIFT+"XXX");
		dto.setBankCountry("United Arab Emirates");
		List<BankResultsDto> resultList = Arrays.asList(dto);
		Mockito.when(routingCodeSearchMWService.fetchBankDetailsWithRoutingCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(resultList);
		Map<String, String> countryMap = new HashedMap<>();
		countryMap.put("AE", "United Arab Emirates");
		//Mockito.when(mobCommonService.getCountryCodeMap()).thenReturn(countryMap);
		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		Mockito.verify(bankRepository,times(1)).getBankCode(Mockito.anyString(), Mockito.eq(MASHREQ_SWIFT+"XXX"), updatedSwift.capture());
		assertEquals(1, response.size());
		assertEquals(accNo, response.get(0).getAccountNo());
		assertEquals(MASHREQ_SWIFT, updatedSwift.getValue());
		
	}
}
