package com.mashreq.transfercoreservice.fundtransfer.strategy;


import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.service.DigitalUserSegment;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import com.mashreq.transfercoreservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static com.mashreq.transfercoreservice.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OwnAccountStrategyTest {

    @InjectMocks
    private OwnAccountStrategy service;
    @Mock
    private AccountBelongsToCifValidator accountBelongsToCifValidator;
    @Mock
    private SameAccountValidator sameAccountValidator;
    @Mock
    private DealValidator dealValidator;
    @Mock
    private FundTransferMWService fundTransferMWService;
    @Mock
    private BalanceValidator balanceValidator;
    @Mock
    private AccountFreezeValidator freezeValidator;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DigitalUserSegment digitalUserSegment;
    @Mock
    private AccountService accountService;
    @Mock
    private MaintenanceService maintenanceService;
    @Mock
    private LimitValidatorFactory limitValidatorFactory;
    @Mock
    private LimitValidator limitValidator;
    @Mock
    private CurrencyValidator currencyValidator;
    @Mock
    PostTransactionService postTransactionService;
    @Mock
    private AsyncUserEventPublisher asyncUserEventPublisher;
    @Mock
    private MinTransactionAmountValidator minTransactionAmountValidator;

    private RequestMetaData metaData = RequestMetaData.builder().build();

    private static final String cif = "012960001";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(service, "localCurrency", "AED");
    }

    @Test
    public void executeBuyGoldTransfer(){
        String fromAccount = "1234567890";
        String toAccount = "0987654321";
        String txnRefNo = "MAC11012267055";

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1234");
        fundTransferRequestDTO.setFromAccount(fromAccount);
        fundTransferRequestDTO.setToAccount(toAccount);
        fundTransferRequestDTO.setCurrency("AED");
        fundTransferRequestDTO.setTxnCurrency("XAU");
        fundTransferRequestDTO.setDestinationAccountCurrency("XAU");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(sameAccountValidator.validate(any(),any())).thenReturn(validationResult);
        when(accountBelongsToCifValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(balanceValidator.validate(any(FundTransferRequestDTO.class),any(),any())).thenReturn(validationResult);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(txnRefNo));
        when(accountService.getAccountsFromCore(any())).thenReturn(getOwnAccountDetails(fromAccount, toAccount, "AED", "XAU"));
        when(accountService.getAccountDetailsFromCore(any())).thenReturn(getCoreAccountDetails().getConnectedAccounts().get(0));
        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.S));

        FundTransferResponse response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
        assertEquals(txnRefNo, response.getTransactionRefNo());
        assertEquals("Own Account Transfer", fundTransferRequestDTO.getPurposeDesc());

        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.F));
        response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
    }

    @Test
    public void executeBuySilverTransfer(){
        String fromAccount = "1234567890";
        String toAccount = "0987654321";
        String txnRefNo = "MAC11012267055";

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1234");
        fundTransferRequestDTO.setFromAccount(fromAccount);
        fundTransferRequestDTO.setToAccount(toAccount);
        fundTransferRequestDTO.setCurrency("AED");
        fundTransferRequestDTO.setTxnCurrency("XAG");
        fundTransferRequestDTO.setDestinationAccountCurrency("XAG");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(sameAccountValidator.validate(any(),any())).thenReturn(validationResult);
        when(accountBelongsToCifValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(balanceValidator.validate(any(FundTransferRequestDTO.class),any(),any())).thenReturn(validationResult);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(txnRefNo));
        when(accountService.getAccountsFromCore(any())).thenReturn(getOwnAccountDetails(fromAccount, toAccount, "AED", "XAG"));
        when(accountService.getAccountDetailsFromCore(any())).thenReturn(getCoreAccountDetails().getConnectedAccounts().get(0));
        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.S));

        FundTransferResponse response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
        assertEquals(txnRefNo, response.getTransactionRefNo());
        assertEquals("Own Account Transfer", fundTransferRequestDTO.getPurposeDesc());

        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.F));
        response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
    }

    @Test
    public void executeSellGoldTransfer(){
        String fromAccount = "1234567890";
        String toAccount = "0987654321";
        String txnRefNo = "MAC11012267055";

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1234");
        fundTransferRequestDTO.setFromAccount(fromAccount);
        fundTransferRequestDTO.setToAccount(toAccount);
        fundTransferRequestDTO.setCurrency("XAU");
        fundTransferRequestDTO.setTxnCurrency("XAU");
        fundTransferRequestDTO.setDestinationAccountCurrency("AED");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(sameAccountValidator.validate(any(),any())).thenReturn(validationResult);
        when(accountBelongsToCifValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(balanceValidator.validate(any(FundTransferRequestDTO.class),any(),any())).thenReturn(validationResult);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(txnRefNo));
        when(accountService.getAccountsFromCore(any())).thenReturn(getOwnAccountDetails(fromAccount, toAccount, "XAU", "AED"));
        when(accountService.getAccountDetailsFromCore(any())).thenReturn(getCoreAccountDetails().getConnectedAccounts().get(0));
        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.S));

        FundTransferResponse response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
        assertEquals(txnRefNo, response.getTransactionRefNo());
        assertEquals("Own Account Transfer", fundTransferRequestDTO.getPurposeDesc());

        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.F));
        response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
    }

    @Test
    public void executeSellSilverTransfer(){
        String fromAccount = "1234567890";
        String toAccount = "0987654321";
        String txnRefNo = "MAC11012267055";

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1234");
        fundTransferRequestDTO.setFromAccount(fromAccount);
        fundTransferRequestDTO.setToAccount(toAccount);
        fundTransferRequestDTO.setCurrency("XAG");
        fundTransferRequestDTO.setTxnCurrency("XAG");
        fundTransferRequestDTO.setDestinationAccountCurrency("AED");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(sameAccountValidator.validate(any(),any())).thenReturn(validationResult);
        when(accountBelongsToCifValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(balanceValidator.validate(any(FundTransferRequestDTO.class),any(),any())).thenReturn(validationResult);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(maintenanceService.convertCurrency(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(txnRefNo));
        when(accountService.getAccountsFromCore(any())).thenReturn(getOwnAccountDetails(fromAccount, toAccount, "XAG", "AED"));
        when(accountService.getAccountDetailsFromCore(any())).thenReturn(getCoreAccountDetails().getConnectedAccounts().get(0));
        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.S));

        FundTransferResponse response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
        assertEquals(txnRefNo, response.getTransactionRefNo());
        assertEquals("Own Account Transfer", fundTransferRequestDTO.getPurposeDesc());

        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.F));
        response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
    }

    @Test
    public void executeAEDTransfer(){
        String fromAccount = "1234567890";
        String toAccount = "0987654321";
        String txnRefNo = "MAC11012267055";

        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setBeneficiaryId("1234");
        fundTransferRequestDTO.setFromAccount(fromAccount);
        fundTransferRequestDTO.setToAccount(toAccount);
        fundTransferRequestDTO.setCurrency("AED");
        fundTransferRequestDTO.setTxnCurrency("AED");
        fundTransferRequestDTO.setDestinationAccountCurrency("AED");

        UserDTO userDTO = new UserDTO();

        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(sameAccountValidator.validate(any(),any())).thenReturn(validationResult);
        when(accountBelongsToCifValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(balanceValidator.validate(any(FundTransferRequestDTO.class),any(),any())).thenReturn(validationResult);
        when(currencyValidator.validate(any(),any(),any())).thenReturn(validationResult);
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(TestUtil.getCurrencyConversionDto());
        when(limitValidator.validate(any(),any(),any(),any(),any())).thenReturn(TestUtil.limitValidatorResultsDto(txnRefNo));
        when(accountService.getAccountsFromCore(any())).thenReturn(getOwnAccountDetails(fromAccount, toAccount, "AED", "AED"));
        when(accountService.getAccountDetailsFromCore(any())).thenReturn(getCoreAccountDetails().getConnectedAccounts().get(0));
        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.S));
        when(minTransactionAmountValidator.validate(any(), any(),any())).thenReturn(validationResult);

        FundTransferResponse response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
        assertEquals(txnRefNo, response.getTransactionRefNo());
        assertEquals("Own Account Transfer", fundTransferRequestDTO.getPurposeDesc());

        when(fundTransferMWService.transfer(any(), any(),eq(txnRefNo))).thenReturn(fundTransferResponse(txnRefNo, MwResponseStatus.F));
        response = service.execute(fundTransferRequestDTO, metaData, userDTO);
        assertNotNull(response);
    }

}
