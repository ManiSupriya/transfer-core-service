package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import com.mashreq.encryption.encryptor.EncryptionService;
import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.transfercoreservice.cache.UserSessionCacheService;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.client.service.*;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CCBalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class WithinAccountEligibilityServiceTest {

	private WithinAccountEligibilityService service;
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
	private AuditEventPublisher userEventPublisher;

	@Before
	public void init() {
		service = new WithinAccountEligibilityService(
				beneficiaryValidator,
				accountService,
				beneficiaryService,
				limitValidatorFactory,
				maintenanceService,
				userEventPublisher);
	}
	
	@Test
	public void test(){

	}



}
