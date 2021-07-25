package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.client.service.QuickRemitService;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class QRAccountEligibilityServiceTest {

	private QRAccountEligibilityService service;
	@Mock
	private BeneficiaryService beneficiaryService;
	@Mock
	private AccountService accountService;
	@Mock
	private MaintenanceService maintenanceService;
	@Mock
	private BeneficiaryValidator beneficiaryValidator;
	@Mock
	private LimitValidatorFactory limitValidatorFactory;
	@Mock
	private CurrencyValidatorFactory currencyValidatorFactory;
	@Mock
	private QuickRemitService quickRemitService;
	@Mock
	private AsyncUserEventPublisher userEventPublisher;
	
	@Before
	public void init() {
		//service = new QRAccountEligibilityService();
	}
	
	@Test
	public void test() {
		QRExchangeResponse response = new QRExchangeResponse();
		response.setAccountCurrency("USD");
		response.setTransactionCurrency("NPR");
		response.setExchangeRate("0.00872600");
		response.setDebitAmountWithoutCharges("43.63");
		response.setTransactionAmount("5000.00");
		service.updateExchangeRateDisplay(response);
		assertEquals("1 USD = 114.60004 NPR",response.getExchangeRateDisplay());
		assertEquals("0.00872600",response.getExchangeRate());
		System.out.println(response.getExchangeRateDisplay());
	}

}
