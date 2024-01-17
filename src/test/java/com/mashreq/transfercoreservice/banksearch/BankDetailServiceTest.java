package com.mashreq.transfercoreservice.banksearch;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

import java.util.*;

import com.mashreq.transfercoreservice.common.LocalIbanValidator;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.OmwCoreClient;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.repository.BankRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
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
	private BankDetailsResolverFactory bankDetailsResolverFactory;
	@Mock
	private IbanBasedBankDetailsResolver ibanBasedBankDetailsResolver;
	@Mock
	private AccountBasedBankDetailsResolver accountBasedBankDetailsResolver;
	@Mock
	private LocalIbanValidator localIbanValidator;


	@BeforeEach
	public void init() {
		LocalIbanValidator localIbanValidator = new LocalIbanValidator("AE", "033", 23, 12);
		service = new BankDetailService( routingCodeSearchMWService, ifscCodeSearchMWService,
				omwClient, bankDetailsMapper, soapServiceProperties, bicCodeSearchService, bankRepository,
				mobCommonService,bankDetailsResolverFactory);


	}
	
	
	@Test
	public void test_accountNumber_returned_for_iban_search() {
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
		BankResultsDto resultsDto = new BankResultsDto();
		List<BankResultsDto> resultsDtos = new ArrayList<>();
		resultsDto.setIbanNumber("AE280330000010698008304");
		resultsDto.setAccountNo(accNo);
		resultsDto.setBankName("Mashreq Bank PSC");
		resultsDto.setSwiftCode("BOMLEADX");
		resultsDtos.add(resultsDto);
		Mockito.when(bankDetailsResolverFactory.getBankDetailsResolver(Mockito.any())).thenReturn(ibanBasedBankDetailsResolver);
		Mockito.when(ibanBasedBankDetailsResolver.getBankDetails(Mockito.any())).thenReturn(resultsDtos);


		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		assertEquals(1, response.size());
		assertEquals(accNo, response.get(0).getAccountNo());
	}

	@Test
	public void test_accountNumber_returned_for_account_search() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("EG");
		request.setType("account");
		request.setValue("0029991234567");
		request.setJourneyType("MT");
		request.setBankCode("0036");


		RequestMetaData metadata = new RequestMetaData();
		metadata.setChannelTraceId("whrvh3b4h5bh6");
		BankDetails bankDetails = new BankDetails();
		bankDetails.setBankCode("0036");
		bankDetails.setBankName("Credit Agricole");
		bankDetails.setSwiftCode("CREDEGCAXXX");
		String accNo = "00000029991234567";
		BankResultsDto resultsDto = new BankResultsDto();
		List<BankResultsDto> resultsDtos = new ArrayList<>();
		resultsDto.setIbanNumber("EG450036000100000029991234567");
		resultsDto.setAccountNo(accNo);
		resultsDto.setBankName("Credit Agricole");
		resultsDto.setSwiftCode("CREDEGCAXXX");
		resultsDtos.add(resultsDto);
		Mockito.when(bankDetailsResolverFactory.getBankDetailsResolver(Mockito.any())).thenReturn(accountBasedBankDetailsResolver);
		Mockito.when(accountBasedBankDetailsResolver.getBankDetails(Mockito.any())).thenReturn(resultsDtos);

		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		assertEquals(1, response.size());
		assertEquals(accNo, response.get(0).getAccountNo());
	}

	@Test
	public void test_iban_returned_for_account_search() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("EG");
		request.setType("account");
		request.setValue("0029991234567");
		request.setJourneyType("MT");
		request.setBankCode("0036");


		RequestMetaData metadata = new RequestMetaData();
		metadata.setChannelTraceId("whrvh3b4h5bh6");
		BankDetails bankDetails = new BankDetails();
		bankDetails.setBankCode("0036");
		bankDetails.setBankName("Credit Agricole");
		bankDetails.setSwiftCode("CREDEGCAXXX");

		BankResultsDto resultsDto = new BankResultsDto();
		List<BankResultsDto> resultsDtos = new ArrayList<>();
		resultsDto.setIbanNumber("EG450036000100000029991234567");
		resultsDto.setBankName("Credit Agricole");
		resultsDto.setSwiftCode("CREDEGCAXXX");
		resultsDtos.add(resultsDto);
		Mockito.when(bankDetailsResolverFactory.getBankDetailsResolver(Mockito.any())).thenReturn(accountBasedBankDetailsResolver);
		Mockito.when(accountBasedBankDetailsResolver.getBankDetails(Mockito.any())).thenReturn(resultsDtos);

		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		assertEquals(1, response.size());
		assertEquals("EG450036000100000029991234567", response.get(0).getIbanNumber());
		assertEquals("CREDEGCAXXX", response.get(0).getSwiftCode());
		assertEquals("Credit Agricole", response.get(0).getBankName());
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
		assertEquals("BOMLEA%", updatedSwift.getValue());
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
		assertEquals("BOMLEA%", updatedSwift.getValue());
		
	}

	/*@Test
	public void test_bank_search_account() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("EG");
		request.setType("account");
		request.setValue("0029991234567");
		request.setBankCode("0036");
		request.setBranchCode("001");
		request.setJourneyType("MT");
		RequestMetaData metadata = new RequestMetaData();
		metadata.setChannelTraceId("whrvh3b4h5bh6");
		BankDetails bankDetails = new BankDetails();
		bankDetails.setBankCode("0036");
		bankDetails.setBankName("Mashreq Bank PSC");
		bankDetails.setBranchCode("0001");
		bankDetails.setSwiftCode("BOMLEADX");

		Mockito.when(bankRepository.findByBankCode("0036")).thenReturn(Optional.of(bankDetails));
		Mockito.when(egyptIbanResolver.constructIBAN(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn("EG450036000100000029991234567");
		Mockito.when(ibanSearchMWService.fetchBankDetailsWithIban(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn();
		List<BankResultsDto> response = service.getBankDetails(request, metadata );
		//assertEquals(1, response.size());
		//assertEquals(accNo, response.get(0).getIbanNumber());
	}*/
}
