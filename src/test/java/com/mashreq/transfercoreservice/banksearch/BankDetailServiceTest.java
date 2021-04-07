package com.mashreq.transfercoreservice.banksearch;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.OmwCoreClient;
import com.mashreq.transfercoreservice.fundtransfer.dto.BankDetails;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.MashreqUAEAccountNumberResolver;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.repository.BankRepository;

@RunWith(MockitoJUnitRunner.class)
public class BankDetailServiceTest {

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
    private MashreqUAEAccountNumberResolver accountNumberResolver;
	
	@Before
	public void init() {
		service = new BankDetailService(ibanSearchMWService, routingCodeSearchMWService, ifscCodeSearchMWService,
				omwClient, bankDetailsMapper, soapServiceProperties, bicCodeSearchService, bankRepository,
				accountNumberResolver);
	}
	
	
	@Test
	public void test_accountNumber() {
		BankDetailRequestDto request = new BankDetailRequestDto();
		request.setCountryCode("AE");
		request.setType("iban");
		request.setValue("AE280330000010698008304");
		RequestMetaData metadata = new RequestMetaData();
		BankDetails bankDetails = new BankDetails();
		bankDetails.setBankCode("033");
		bankDetails.setBankName("Mashreq Bank PSC");
		bankDetails.setSwiftCode("BOMLEADXXX");
		String accNo = "010698008304";
		Mockito.when(bankRepository.findByBankCode("033")).thenReturn(Optional.of(bankDetails));
		Mockito.when(accountNumberResolver.generateAccountNumber(Mockito.anyString())).thenReturn(accNo);
		List<BankResultsDto> response = service.getBankDetails("whrvh3b4h5bh6", request, metadata );
		assertEquals(1, response.size());
		assertEquals(accNo, response.get(0).getAccountNo());
	}

}
