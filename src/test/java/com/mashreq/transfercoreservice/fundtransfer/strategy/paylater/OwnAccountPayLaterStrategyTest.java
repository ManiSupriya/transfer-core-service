package com.mashreq.transfercoreservice.fundtransfer.strategy.paylater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountTypeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.notification.service.DigitalUserSegment;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;
import com.mashreq.transfercoreservice.paylater.utils.SequenceNumberGenerator;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class OwnAccountPayLaterStrategyTest {
	private OwnAccountPayLaterStrategy payLaterStrategy;
	@Mock
	private AccountBelongsToCifValidator accountBelongsToCifValidator;
	@Mock
	private SameAccountValidator sameAccountValidator;
	@Mock
	private CurrencyValidator currencyValidator;
	@Mock
	private LimitValidator limitValidator;
	@Mock
	private AccountService accountService;
	@Mock
	private DealValidator dealValidator;
	@Mock
	private MaintenanceService maintenanceService;
	@Mock
	private FundTransferMWService fundTransferMWService;
	@Mock
	private BalanceValidator balanceValidator;
	@Mock
	private NotificationService notificationService; 
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	@Mock
	private DigitalUserSegment digitalUserSegment;
	@Mock
	private AccountFreezeValidator freezeValidator;
	@Mock
	private FundTransferOrderRepository fundTransferOrderRepository;
	@Mock
	private PostTransactionService postTransactionService;
	@Mock
	private SequenceNumberGenerator seqGenerator;
	@Mock
	private MinTransactionAmountValidator minTransactionAmountValidator;
	
	@BeforeEach
	public void init() {
		payLaterStrategy = new OwnAccountPayLaterStrategy(accountBelongsToCifValidator,sameAccountValidator,
				currencyValidator,limitValidator,accountService,
				dealValidator,maintenanceService,fundTransferMWService,balanceValidator,
				notificationService,auditEventPublisher,digitalUserSegment,freezeValidator,
				postTransactionService,
				fundTransferOrderRepository,seqGenerator, minTransactionAmountValidator);
		ReflectionTestUtils.setField(payLaterStrategy, "localCurrency", "AED");
	}
	
	@Test
	public void test_successScnenario() {
		FundTransferRequestDTO request = FundTransferTestUtil.generateFundTransferRequest();
		request.setChargeBearer(null);
		request.setServiceType(ServiceType.WYMA.getName());
		request.setOrderType(FTOrderType.PL.getName());
		request.setStartDate("2041-12-12");
		RequestMetaData metadata = FundTransferTestUtil.getMetadata();
		UserDTO userDTO = FundTransferTestUtil.getUserDTO();
		ValidationResult validationResult = ValidationResult.builder().success(true).build();
		Mockito.when(sameAccountValidator.validate(Mockito.eq(request), Mockito.eq(metadata))).thenReturn(validationResult);
		Mockito.when(accountBelongsToCifValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(freezeValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		String transactionRefNo = "TRN-test-12234";
		LimitValidatorResponse limitResponse = LimitValidatorResponse.builder().transactionRefNo(transactionRefNo).build();
		Mockito.when(limitValidator.validate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(limitResponse );
		List<AccountDetailsDTO> accountList  = FundTransferTestUtil.generateAccountsList(metadata,request);
		Mockito.when(currencyValidator.validate(Mockito.eq(request), Mockito.eq(metadata), Mockito.any())).thenReturn(validationResult);
		Mockito.when(accountService.getAccountsFromCore(Mockito.eq(metadata.getPrimaryCif()))).thenReturn(accountList);
		CurrencyConversionDto conversionResult = FundTransferTestUtil.getConversionResult(request);
		Mockito.when(maintenanceService.convertBetweenCurrencies(Mockito.any())).thenReturn(conversionResult );
		SearchAccountDto accountDto = new SearchAccountDto();
		SearchAccountTypeDto accountType = new SearchAccountTypeDto();
		accountType.setAccountType("MBMETA");
		accountDto.setAccountType(accountType );
		Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(accountDto );
		when(minTransactionAmountValidator.validate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(validationResult);

		//Mockito.when(seqGenerator.getNextOrderId()).thenReturn("210512344321");
		FundTransferResponse response = payLaterStrategy.execute(request, metadata, userDTO);
		assertEquals(transactionRefNo, response.getTransactionRefNo());
		assertEquals(conversionResult.getAccountCurrencyAmount(), response.getDebitAmount());
		assertTrue(response.isPayOrderInitiated());
	}
}
