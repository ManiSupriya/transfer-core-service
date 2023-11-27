package com.mashreq.transfercoreservice.fundtransfer.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mashreq.transfercoreservice.config.EscrowConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountTypeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.repository.EscrowAccountRepository;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.AccountNumberResolver;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.AccountFreezeValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BalanceValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CCTransactionEligibilityValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.CurrencyValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.DealValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.MinTransactionAmountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.SameAccountValidator;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.model.EscrowAccountDetails;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;

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
    private EscrowAccountRepository escrowAccountRepository;
    @Mock
    private EscrowConfig escrowConfig;
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
        SearchAccountTypeDto searchAccountTypeDto = new SearchAccountTypeDto();
        searchAccountTypeDto.setAccountType("ITRSTN");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        searchAccountDto.setAccountType(searchAccountTypeDto);
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
        Mockito.when(escrowConfig.isEnabled()).thenReturn(false);

        Mockito.when(fundTransferMWService.transfer(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
        Mockito.doNothing().when(notificationService).sendNotifications(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doNothing().when(postTransactionService).performPostTransactionActivities(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any());
        FundTransferResponse fundTransferResponse = withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
        Assert.assertNotNull(fundTransferResponse);
    }
    @Test
    public void test_success_trust_account() {
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
        SearchAccountTypeDto searchAccountTypeDto = new SearchAccountTypeDto();
        searchAccountTypeDto.setAccountType("ITRSTN");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        searchAccountDto.setAccountType(searchAccountTypeDto);
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto).build();
        EscrowAccountDetails escrowAccountDetails = new EscrowAccountDetails();
        escrowAccountDetails.setAccountNo("1234");
        escrowAccountDetails.setProjectName("1234");
        Optional<EscrowAccountDetails> escrowAccounts = Optional.of(escrowAccountDetails);
        List<String> trustAccounts = new ArrayList<>();
        trustAccounts.add("ITRSTN");
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
        Mockito.when(escrowAccountRepository.findByAccountNo(Mockito.any())).thenReturn(escrowAccounts);
        Mockito.when(escrowConfig.getTrustAccounts()).thenReturn(trustAccounts);
        Mockito.when(escrowConfig.isEnabled()).thenReturn(true);

        Mockito.when(fundTransferMWService.transfer(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
        Mockito.doNothing().when(notificationService).sendNotifications(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doNothing().when(postTransactionService).performPostTransactionActivities(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any());
        FundTransferResponse fundTransferResponse = withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
        Assert.assertNotNull(fundTransferResponse);
    }
    @Test
    public void test_success_oaAccount() {
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
        SearchAccountTypeDto searchAccountTypeDto = new SearchAccountTypeDto();
        searchAccountTypeDto.setAccountType("ITROWA");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        searchAccountDto.setAccountType(searchAccountTypeDto);
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto).build();
        EscrowAccountDetails escrowAccountDetails = new EscrowAccountDetails();
        escrowAccountDetails.setAccountNo("1234");
        escrowAccountDetails.setProjectName("1234");
        Optional<EscrowAccountDetails> escrowAccounts = Optional.of(escrowAccountDetails);
        List<String> oaAccounts = new ArrayList<>();
        oaAccounts.add("ITROWA");
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
        Mockito.when(escrowAccountRepository.findByAccountNo(Mockito.any())).thenReturn(escrowAccounts);
        Mockito.when(escrowConfig.isEnabled()).thenReturn(true);
        Mockito.when(escrowConfig.getOaAccounts()).thenReturn(oaAccounts);
        Mockito.when(fundTransferMWService.transfer(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
        Mockito.doNothing().when(notificationService).sendNotifications(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doNothing().when(postTransactionService).performPostTransactionActivities(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any());
        FundTransferResponse fundTransferResponse = withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
        Assert.assertNotNull(fundTransferResponse);
    }

    @Test
    public void test_success_other_account() {
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
        SearchAccountTypeDto searchAccountTypeDto = new SearchAccountTypeDto();
        searchAccountTypeDto.setAccountType("ESYPVCUST");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        searchAccountDto.setAccountType(searchAccountTypeDto);
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto).build();
        EscrowAccountDetails escrowAccountDetails = new EscrowAccountDetails();
        escrowAccountDetails.setAccountNo("1234");
        escrowAccountDetails.setProjectName("1234");
        Optional<EscrowAccountDetails> escrowAccounts = Optional.of(escrowAccountDetails);
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
        Mockito.when(escrowAccountRepository.findByAccountNo(Mockito.any())).thenReturn(escrowAccounts);
        Mockito.when(escrowConfig.isEnabled()).thenReturn(true);

        Mockito.when(fundTransferMWService.transfer(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
        Mockito.doNothing().when(notificationService).sendNotifications(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doNothing().when(postTransactionService).performPostTransactionActivities(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any());
        FundTransferResponse fundTransferResponse = withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
        Assert.assertNotNull(fundTransferResponse);
    }

    @Test
    public void test_success_no_account_inDB() {
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
        SearchAccountTypeDto searchAccountTypeDto = new SearchAccountTypeDto();
        searchAccountTypeDto.setAccountType("ESYPVCUST");
        SearchAccountDto searchAccountDto = new SearchAccountDto();
        searchAccountDto.setCurrency("EGP");
        searchAccountDto.setAccountType(searchAccountTypeDto);
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto).build();
        EscrowAccountDetails escrowAccountDetails = new EscrowAccountDetails();
        escrowAccountDetails.setAccountNo("1234");
        escrowAccountDetails.setProjectName("1234");
        Optional<EscrowAccountDetails> escrowAccounts = Optional.of(escrowAccountDetails);
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
        Mockito.when(escrowAccountRepository.findByAccountNo(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(escrowConfig.isEnabled()).thenReturn(true);

        Mockito.when(fundTransferMWService.transfer(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
        Mockito.doNothing().when(notificationService).sendNotifications(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doNothing().when(postTransactionService).performPostTransactionActivities(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any());
        FundTransferResponse fundTransferResponse = withinMashreqStrategy.execute(requestDTO,requestMetaData, userDTO);

        //Then
        Assert.assertNotNull(fundTransferResponse);
    }
}
