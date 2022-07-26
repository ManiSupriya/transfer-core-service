package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.AccountNumberResolver;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class WithinMashreqStrategyTest {

    @InjectMocks
    private WithinMashreqStrategy withinMashreqStrategy;
    @Mock
    private SameAccountValidator sameAccountValidator;
    @Mock
    private AccountBelongsToCifValidator accountBelongsToCifValidator;
    @Mock
    private CurrencyValidator currencyValidator;
    @Mock
    private BeneficiaryValidator beneficiaryValidator;
    @Mock
    private AccountService accountService;
    @Mock
    private BeneficiaryService beneficiaryService;
    @Mock
    private LimitValidator limitValidator;
    @Mock
    private MaintenanceService maintenanceService;
    @Mock
    private FundTransferMWService fundTransferMWService;
    @Mock
    private BalanceValidator balanceValidator;
    @Mock
    private DealValidator dealValidator;
    @Mock
    private AsyncUserEventPublisher auditEventPublisher;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AccountFreezeValidator freezeValidator;
    @Mock
    private AccountNumberResolver accountNumberResolver;
    @Mock
    private PostTransactionService postTransactionService;
    @Mock
    private CCTransactionEligibilityValidator ccTrxValidator;
    @Mock
    private MinTransactionAmountValidator minTransactionAmountValidator;

    @Test(expected = GenericException.class)
    public void test_failure_due_to_deal() {
        //Given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setFromAccount("acc123");
        requestDTO.setCurrency("EGP");
        requestDTO.setTxnCurrency("EGP");
        requestDTO.setBeneficiaryId("23");
        requestDTO.setAmount(new BigDecimal(500));
        requestDTO.setDealNumber("deal123");
        RequestMetaData requestMetaData = new RequestMetaData();
        UserDTO userDTO = new UserDTO();
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        final List<AccountDetailsDTO> accountsFromCore = new ArrayList<>();
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setNumber("acc123");
        accountsFromCore.add(accountDetailsDTO);
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        LimitValidatorResponse limitValidatorResponse = new LimitValidatorResponse();
        limitValidatorResponse.setTransactionRefNo("txn1234");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        //When
        ReflectionTestUtils.setField(withinMashreqStrategy, "localCurrency", "EGP");
        Mockito.when(ccTrxValidator.validate(Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(sameAccountValidator.validate(Mockito.any(),Mockito.any())).thenReturn(validationResult);

        Mockito.when(accountBelongsToCifValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(accountService.getAccountsFromCore(Mockito.any())).thenReturn(accountsFromCore);
        Mockito.when(beneficiaryService.getByIdWithoutValidation(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(beneficiaryDto);
        Mockito.when(beneficiaryValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);

        Mockito.when(freezeValidator.validate(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(currencyValidator.validate(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(balanceValidator.validate((FundTransferRequestDTO) Mockito.any(), Mockito.eq(requestMetaData), Mockito.any())).thenReturn(validationResult);
        Mockito.when(maintenanceService.convertCurrency(Mockito.any())).thenReturn(new CurrencyConversionDto());
        Mockito.when(minTransactionAmountValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(limitValidator.validate(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(limitValidatorResponse);
        withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
    }

    @Test
    public void test_success() {
        //Given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setFromAccount("acc123");
        requestDTO.setCurrency("EGP");
        requestDTO.setTxnCurrency("EGP");
        requestDTO.setBeneficiaryId("23");
        requestDTO.setAmount(new BigDecimal(500));

        RequestMetaData requestMetaData = new RequestMetaData();
        UserDTO userDTO = new UserDTO();
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        final List<AccountDetailsDTO> accountsFromCore = new ArrayList<>();
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setNumber("acc123");
        accountDetailsDTO.setCurrency("EGP");
        accountsFromCore.add(accountDetailsDTO);
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        LimitValidatorResponse limitValidatorResponse = new LimitValidatorResponse();
        limitValidatorResponse.setTransactionRefNo("txn1234");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto).build();
        //When
        ReflectionTestUtils.setField(withinMashreqStrategy, "localCurrency", "EGP");
        Mockito.when(ccTrxValidator.validate(Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(sameAccountValidator.validate(Mockito.any(),Mockito.any())).thenReturn(validationResult);

        Mockito.when(accountBelongsToCifValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(accountService.getAccountsFromCore(Mockito.any())).thenReturn(accountsFromCore);
        Mockito.when(beneficiaryService.getByIdWithoutValidation(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(beneficiaryDto);
        Mockito.when(beneficiaryValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);

        Mockito.when(freezeValidator.validate(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(currencyValidator.validate(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(balanceValidator.validate((FundTransferRequestDTO) Mockito.any(), Mockito.eq(requestMetaData), Mockito.any())).thenReturn(validationResult);
        Mockito.when(minTransactionAmountValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(validationResult);
        Mockito.when(limitValidator.validate(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(limitValidatorResponse);

        Mockito.when(fundTransferMWService.transfer(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
        Mockito.doNothing().when(notificationService).sendNotifications(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doNothing().when(postTransactionService).performPostTransactionActivities(Mockito.any(),Mockito.any(),Mockito.any());
        FundTransferResponse fundTransferResponse = withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
        Assert.assertNotNull(fundTransferResponse);
    }


}
