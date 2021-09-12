package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.service.*;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CCBalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;

@RunWith(MockitoJUnitRunner.class)
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
	private AuditEventPublisher userEventPublisher;

	@Mock
	private CCBalanceValidator ccBalanceValidator;
	@Mock
	private QRDealsService qrDealsService;

	@Mock
	private UserSessionCacheService userSessionCacheService;
	@Mock
	private CardService cardService;

	private EncryptionService encryptionService = new EncryptionService();
	
	@Before
	public void init() {
		service = new QRAccountEligibilityService(
				beneficiaryService,
				accountService,
				maintenanceService,
				beneficiaryValidator,
				limitValidatorFactory,
				currencyValidatorFactory,
				quickRemitService,
				ccBalanceValidator,
				qrDealsService,
				userEventPublisher,
				userSessionCacheService,
				cardService);
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
