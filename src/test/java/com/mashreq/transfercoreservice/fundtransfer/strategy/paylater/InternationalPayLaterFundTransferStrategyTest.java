package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.PaymentPurposeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;
import com.mashreq.transfercoreservice.paylater.utils.SequenceNumberGenerator;

@RunWith(MockitoJUnitRunner.class)
public class InternationalPayLaterFundTransferStrategyTest {
	private InternationalPayLaterFundTransferStrategy internationalPayLaterFundTransferStrategy;
	@Mock
	private AccountService accountService;
	@Mock
	private AccountBelongsToCifValidator accountBelongsToCifValidator;
	@Mock
	private PaymentPurposeValidator paymentPurposeValidator;
	@Mock
	private BeneficiaryValidator beneficiaryValidator;
	@Mock
	private BalanceValidator balanceValidator;
	@Mock
	private FundTransferMWService fundTransferMWService;
	@Mock
	private MaintenanceService maintenanceService;
	@Mock
	private MobCommonService mobCommonService;
	@Mock
	private DealValidator dealValidator;
	@Mock
	private NotificationService notificationService;
	@Mock
	private BeneficiaryService beneficiaryService;
	@Mock
	private LimitValidator limitValidator;
	@Mock
	private FundTransferOrderRepository fundTransferOrderRepository;
	@Mock
    private PostTransactionService postTransactionService;
	@Mock
	private SequenceNumberGenerator seqGenerator;
	@Mock
	private CCTransactionEligibilityValidator ccTrxValidator;
	@Mock
	private CurrencyValidator currencyValidator;
	
	@Before
	public void init () {
		internationalPayLaterFundTransferStrategy = new InternationalPayLaterFundTransferStrategy( accountService, accountBelongsToCifValidator, paymentPurposeValidator, beneficiaryValidator,
				balanceValidator, fundTransferMWService, maintenanceService, mobCommonService, dealValidator,
				notificationService, beneficiaryService, limitValidator,ccTrxValidator,
				fundTransferOrderRepository,seqGenerator, currencyValidator);
		ReflectionTestUtils.setField(internationalPayLaterFundTransferStrategy,"postTransactionService", postTransactionService);
		ReflectionTestUtils.setField(internationalPayLaterFundTransferStrategy, "localCurrency", "AED");
		
		when(currencyValidator.validate(any(), any(), any())).thenReturn(ValidationResult.builder().success(true).build());
	}
	
	@Test
	public void test() {
		FundTransferRequestDTO request = FundTransferTestUtil.generateFundTransferRequest();
		request.setServiceType(ServiceType.INFT.getName());
		request.setOrderType(FTOrderType.PL.getName());
		request.setJourneyVersion("V2");
		request.setStartDate("2041-12-12");
		String beneficiaryId = "123";
		request.setBeneficiaryId(beneficiaryId );
		RequestMetaData metadata = FundTransferTestUtil.getMetadata();
		UserDTO userDTO = FundTransferTestUtil.getUserDTO();
		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		Mockito.when(accountBelongsToCifValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(paymentPurposeValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(beneficiaryValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(ccTrxValidator.validate(Mockito.any(), Mockito.any())).thenReturn(validationResult);
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setId(Long.valueOf(beneficiaryId));
		Mockito.when(beneficiaryService.getById(Mockito.eq(Long.valueOf(request.getBeneficiaryId())), Mockito.eq("V2"),Mockito.eq(metadata),Mockito.eq("international"))).thenReturn(beneficiaryDto);
		String transactionRefNo = "TRN-test-12234";
		LimitValidatorResponse limitResponse = LimitValidatorResponse.builder().transactionRefNo(transactionRefNo).build();
		Mockito.when(limitValidator.validate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(limitResponse );
		List<AccountDetailsDTO> accountList  = FundTransferTestUtil.generateAccountsList(metadata,request);
		Mockito.when(accountService.getAccountsFromCore(Mockito.eq(metadata.getPrimaryCif()))).thenReturn(accountList);
		CurrencyConversionDto conversionResult = FundTransferTestUtil.getConversionResult(request);
		Mockito.when(maintenanceService.convertBetweenCurrencies(Mockito.any())).thenReturn(conversionResult );
		FundTransferResponse response = internationalPayLaterFundTransferStrategy.execute(request, metadata, userDTO);
		assertEquals(transactionRefNo, response.getTransactionRefNo());
		assertEquals(conversionResult.getAccountCurrencyAmount(), response.getDebitAmount());
		assertTrue(response.isPayOrderInitiated());
	}

}
