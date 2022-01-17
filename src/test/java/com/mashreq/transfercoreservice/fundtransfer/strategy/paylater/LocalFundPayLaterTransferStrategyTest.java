package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferCCMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCBalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.IBANValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.PaymentPurposeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;
import com.mashreq.transfercoreservice.paylater.utils.SequenceNumberGenerator;
import com.mashreq.transfercoreservice.repository.CountryRepository;

@RunWith(MockitoJUnitRunner.class)
public class LocalFundPayLaterTransferStrategyTest {
	private LocalFundPayLaterTransferStrategy localFundPayLaterTransferStrategy;
	@Mock
	private IBANValidator ibanValidator;
	@Mock
	private AccountBelongsToCifValidator accountBelongsToCifValidator;
	@Mock
	private CCBelongsToCifValidator ccBelongsToCifValidator;
	@Mock
	private BeneficiaryValidator beneficiaryValidator;
	@Mock
	private AccountService accountService;
	@Mock
	private BeneficiaryService beneficiaryService;
	@Mock
	private LimitValidator limitValidator;
	@Mock
	private FundTransferMWService fundTransferMWService;
	@Mock
	private PaymentPurposeValidator paymentPurposeValidator;
	@Mock
	private BalanceValidator balanceValidator;
	@Mock
	private CCBalanceValidator ccBalanceValidator;
	@Mock
	private MaintenanceService maintenanceService;
	@Mock
	private MobCommonService mobCommonService;
	@Mock
	private DealValidator dealValidator;
	@Mock
	private CountryRepository countryRepository;
	@Mock
	private FundTransferCCMWService fundTransferCCMWService;
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	@Mock
	private NotificationService notificationService;
	@Mock
	private FundTransferOrderRepository fundTransferOrderRepository;
	@Mock
	private QRDealsService qrDealsService;
	@Mock
    private CardService cardService;
	@Mock
    private PostTransactionService postTransactionService;
	@Mock
	private SequenceNumberGenerator seqGenerator;
	@Mock
	private CCTransactionEligibilityValidator ccTrxValidator;
	@Before
	public void init() {
		localFundPayLaterTransferStrategy = new  LocalFundPayLaterTransferStrategy(ibanValidator, accountBelongsToCifValidator, ccBelongsToCifValidator, beneficiaryValidator,
				accountService, beneficiaryService, limitValidator, fundTransferMWService, paymentPurposeValidator,
				balanceValidator, ccBalanceValidator, maintenanceService, mobCommonService, dealValidator, countryRepository,
				fundTransferCCMWService, auditEventPublisher, notificationService,qrDealsService, cardService, postTransactionService, fundTransferOrderRepository,
				seqGenerator,ccTrxValidator);
		 ReflectionTestUtils.setField(localFundPayLaterTransferStrategy,"cardService", cardService);
	     ReflectionTestUtils.setField(localFundPayLaterTransferStrategy,"qrDealsService", qrDealsService);
	     ReflectionTestUtils.setField(localFundPayLaterTransferStrategy,"postTransactionService", postTransactionService);
	     ReflectionTestUtils.setField(localFundPayLaterTransferStrategy,"address", "test address");
	     ReflectionTestUtils.setField(localFundPayLaterTransferStrategy,"localCurrency", "AED");
	}
	
	@Test
	public void test_successScenario() {
		FundTransferRequestDTO request = FundTransferTestUtil.generateFundTransferRequest();
		request.setServiceType(ServiceType.LOCAL.getName());
		request.setOrderType(FTOrderType.PL.getName());
		request.setStartDate("2041-12-12");
		String beneficiaryId = "123";
		request.setBeneficiaryId(beneficiaryId );
		RequestMetaData metadata = FundTransferTestUtil.getMetadata();
		UserDTO userDTO = FundTransferTestUtil.getUserDTO();
		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		Mockito.when(accountBelongsToCifValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(paymentPurposeValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(beneficiaryValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(ibanValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(ccTrxValidator.validate(Mockito.any(), Mockito.any())).thenReturn(validationResult);
		String transactionRefNo = "TRN-test-12234";
		LimitValidatorResponse limitResponse = LimitValidatorResponse.builder().transactionRefNo(transactionRefNo).build();
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setId(Long.valueOf(beneficiaryId));
		Mockito.when(beneficiaryService.getByIdWithoutValidation(Mockito.eq(metadata.getPrimaryCif()), Mockito.eq(Long.valueOf(request.getBeneficiaryId())), any(),Mockito.eq(metadata))).thenReturn(beneficiaryDto);
		Mockito.when(limitValidator.validate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(limitResponse );
		List<AccountDetailsDTO> accountList  = FundTransferTestUtil.generateAccountsList(metadata,request);
		Mockito.when(accountService.getAccountsFromCore(Mockito.eq(metadata.getPrimaryCif()))).thenReturn(accountList);
		CurrencyConversionDto conversionResult = FundTransferTestUtil.getConversionResult(request);
		Mockito.when(maintenanceService.convertBetweenCurrencies(Mockito.any())).thenReturn(conversionResult);
		//Mockito.when(seqGenerator.getNextOrderId()).thenReturn("210512344321");
		FundTransferResponse response = localFundPayLaterTransferStrategy.execute(request, metadata, userDTO);
		assertEquals(transactionRefNo, response.getTransactionRefNo());
		assertEquals(request.getAmount(), response.getDebitAmount());
		assertTrue(response.isPayOrderInitiated());
	}

}
