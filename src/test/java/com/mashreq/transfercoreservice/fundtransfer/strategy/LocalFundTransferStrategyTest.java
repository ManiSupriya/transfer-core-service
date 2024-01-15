package com.mashreq.transfercoreservice.fundtransfer.strategy;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.CardService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.service.QRDealsService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import com.mashreq.transfercoreservice.notification.service.NotificationService;
import com.mashreq.transfercoreservice.notification.service.PostTransactionService;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalFundTransferStrategyTest {

    @InjectMocks
    private LocalFundTransferStrategy localFundTransferStrategy;

    @Mock
    private  IBANValidator ibanValidator;

    @Mock
    private  AccountBelongsToCifValidator accountBelongsToCifValidator;

    @Mock
    private  BeneficiaryValidator beneficiaryValidator;

    @Mock
    private  AccountService accountService;

    @Mock
    private  BeneficiaryService beneficiaryService;

    @Mock
    private  LimitValidator limitValidator;

    @Mock
    private  FundTransferMWService fundTransferMWService;

    @Mock
    private  PaymentPurposeValidator paymentPurposeValidator;

    @Mock
    private  BalanceValidator balanceValidator;

    @Mock
    private  MaintenanceService maintenanceService;

    @Mock
    private MobCommonService mobCommonService;
    
    @Mock
    private DealValidator dealValidator;

    @Mock
    private QRDealsService qrDealsService;

    @Mock
    private CardService cardService;

    @Mock
    private CCBalanceValidator ccBalanceValidator;

    @Mock
    private CCBelongsToCifValidator ccBelongsToCifValidator;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PostTransactionService postTransactionService;
    
    @Mock
    private CurrencyValidator currencyValidator;

    @Mock
    private CCTransactionEligibilityValidator ccTransactionEligibilityValidator;

    @Mock
    private MinTransactionAmountValidator minTransactionAmountValidator;

    @Captor
    private ArgumentCaptor<FundTransferRequest> fundTransferRequest;
    
    @BeforeEach
    public void init() {
    	when(currencyValidator.validate(any(), any(), any())).thenReturn(ValidationResult.builder().success(true).build());
    }

    @Test
    public void test_when_fund_transfer_is_successful_when_source_destination_currency_same() {
        //Given
        BigDecimal limitUsageAmount = new BigDecimal(200);
        BigDecimal paidAmt = new BigDecimal(200);

        String limitVersionUuid = "uuid1234";
        String fromAcct = "019010050532";
        String toAcct = "AE120260001015673975601";
        String channel = "MOB";
        String channelTraceId = "traceId123";
        String cif = "12345";
        String beneId = "121";
        String srcCurrency = "AED";
        String productId = "DBLC";
        String purposeDesc = "Salary";
        String purposeCode = "SAL";
        String chargeBearer = "O";
        String finTxnNo = "fin123";
        String branchCode = "083";
        String fullName = "Deepa Shivakumar";
        String swift = "EBILAEADXXX";
        String bankName = "EMIRATES NBD PJSC";
        String address = "UNITED ARAB EMIRATES";

        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount(toAcct);
        requestDTO.setFromAccount(fromAcct);
        requestDTO.setPurposeDesc(purposeDesc);
        requestDTO.setChargeBearer(chargeBearer);
        requestDTO.setPurposeCode(purposeCode);
        requestDTO.setFinTxnNo(finTxnNo);
        requestDTO.setAmount(new BigDecimal(200));
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setBeneficiaryId(beneId);
        //requestDTO.setCardNo("393D9E6606F972B3F057CD932BFAA2615DFB187EECEC8860E7B35C71EC4F73F7");

        RequestMetaData metadata =  RequestMetaData.builder().primaryCif(cif).channel(channel).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setSwiftCode(swift);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setFullName(fullName);
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setNumber(fromAcct);
        accountDetailsDTO.setCurrency(srcCurrency);
        accountDetailsDTO.setBranchCode(branchCode);
        final List<AccountDetailsDTO> accountsFromCore = new ArrayList<>();
        accountsFromCore.add(accountDetailsDTO);
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));

        CurrencyConversionDto secondConversion = new CurrencyConversionDto();
        secondConversion.setAccountCurrencyAmount(paidAmt);

        CoreFundTransferResponseDto coreResponse = new CoreFundTransferResponseDto();
        coreResponse.setMwResponseStatus(MwResponseStatus.S);
        //when

        ReflectionTestUtils.setField(localFundTransferStrategy,"localCurrency", srcCurrency);
        ReflectionTestUtils.setField(localFundTransferStrategy,"address", address);
        //ReflectionTestUtils.setField(localFundTransferStrategy,"transactionCode", "015");

        final ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(validationResult);
        when(accountService.getAccountsFromCore(eq(metadata.getPrimaryCif()))).thenReturn(accountsFromCore);
        when(mobCommonService.getPaymentPurposes( eq("LOCAL"), eq(""), eq("I"))).thenReturn(popList);

        when(accountBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(beneficiaryService.getByIdWithoutValidation(eq(metadata.getPrimaryCif()), eq(Long.valueOf(beneId)), any(), any())).thenReturn(beneficiaryDto);
        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ibanValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(balanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setLimitVersionUuid(limitVersionUuid);
        LimitValidatorResponse limitValidatorResponse = new LimitValidatorResponse();
        limitValidatorResponse.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate( any(),  any(),  any(),  any(), any()))
        .thenReturn(limitValidatorResponse);
        //when(dealValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        //when(ccBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        //when(ccBalanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ccTransactionEligibilityValidator.validate(any(), any())).thenReturn(ValidationResult.builder().success(true).build());
        //when(qrDealsService.getQRDealDetails(any(), any())).thenReturn(new QRDealDetails());
        when(maintenanceService.convertBetweenCurrencies(any())).thenReturn(secondConversion);

        when(fundTransferMWService.transfer(fundTransferRequest.capture(),any(),any()))
                .thenReturn(FundTransferResponse.builder().responseDto(coreResponse).limitUsageAmount(limitUsageAmount).limitVersionUuid(limitVersionUuid).build());
        when(minTransactionAmountValidator.validate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(validationResult);


        final FundTransferResponse response = localFundTransferStrategy.execute(requestDTO, metadata, userDTO);
        final FundTransferRequest actualFundTransferRequest = fundTransferRequest.getValue();

        //then
       assertEquals(limitVersionUuid,response.getLimitVersionUuid());
       assertEquals(limitUsageAmount,response.getLimitUsageAmount());
       assertEquals(actualFundTransferRequest.getAmount(),limitUsageAmount);
       assertEquals(actualFundTransferRequest.getProductId(),productId);
       assertEquals(actualFundTransferRequest.getChannel(),channel);
       assertEquals(actualFundTransferRequest.getChannelTraceId(),channelTraceId);
       assertEquals(actualFundTransferRequest.getFromAccount(),fromAcct);
       assertEquals(actualFundTransferRequest.getToAccount(),toAcct);
       assertEquals(actualFundTransferRequest.getPurposeCode(),purposeCode);
       assertEquals(actualFundTransferRequest.getPurposeDesc(),purposeDesc);
       assertEquals(actualFundTransferRequest.getChargeBearer(),chargeBearer);
       assertEquals(actualFundTransferRequest.getFinTxnNo(),finTxnNo);
       assertEquals(actualFundTransferRequest.getSourceCurrency(),srcCurrency);
       assertEquals(actualFundTransferRequest.getSourceBranchCode(),branchCode);
       assertEquals(actualFundTransferRequest.getBeneficiaryFullName(),fullName);
       assertEquals(actualFundTransferRequest.getDestinationCurrency(),srcCurrency);
       assertEquals(actualFundTransferRequest.getAwInstBICCode(),swift);
       assertEquals(actualFundTransferRequest.getAwInstName(),bankName);
       assertEquals(actualFundTransferRequest.getBeneficiaryBankCountry(),address);
       assertEquals("U",actualFundTransferRequest.getPostingGroup());
    }

    @Test
    public void test_when_fund_transfer_is_successful_when_source_destination_currency_different() {
        //Given
        BigDecimal paidAmt = new BigDecimal(200);
        BigDecimal paidAmtInSrcCurrency = BigDecimal.valueOf(54.45);
        String limitVersionUuid = "uuid1234";
        String fromAcct = "019010050532";
        String toAcct = "AE120260001015673975601";
        String channel = "MOB";
        String channelTraceId = "traceId123";
        String cif = "12345";
        String beneId = "121";
        String srcCurrency = "USD";
        String destCurrency = "AED";
        String productId = "DBLC";
        String purposeDesc = "Salary";
        String purposeCode = "SAL";
        String chargeBearer = "O";
        String finTxnNo = "fin123";
        String branchCode = "083";
        String fullName = "Deepa Shivakumar";
        String swift = "EBILAEADXXX";
        String bankName = "EMIRATES NBD PJSC";
        String address = "UNITED ARAB EMIRATES";

        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount(toAcct);
        requestDTO.setFromAccount(fromAcct);
        requestDTO.setPurposeDesc(purposeDesc);
        requestDTO.setChargeBearer(chargeBearer);
        requestDTO.setPurposeCode(purposeCode);
        requestDTO.setFinTxnNo(finTxnNo);
        requestDTO.setAmount(paidAmt);
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setBeneficiaryId(beneId);
        //requestDTO.setCardNo("393D9E6606F972B3F057CD932BFAA2615DFB187EECEC8860E7B35C71EC4F73F7");

        RequestMetaData metadata =  RequestMetaData.builder().primaryCif(cif).channel(channel).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setSwiftCode(swift);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setFullName(fullName);
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        accountDetailsDTO.setNumber(fromAcct);
        accountDetailsDTO.setCurrency(srcCurrency);
        accountDetailsDTO.setBranchCode(branchCode);
        final List<AccountDetailsDTO> accountsFromCore = new ArrayList<>();
        accountsFromCore.add(accountDetailsDTO);
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));


        CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(fromAcct);
        currencyRequest.setAccountCurrency(srcCurrency);
        currencyRequest.setTransactionCurrency(destCurrency);
        currencyRequest.setTransactionAmount(paidAmt);
        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(paidAmtInSrcCurrency);

        CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDto.setAccountNumber(fromAcct);
        currencyConversionRequestDto.setAccountCurrency(srcCurrency);
        currencyConversionRequestDto.setAccountCurrencyAmount(paidAmtInSrcCurrency);
        currencyConversionRequestDto.setDealNumber(null);
        currencyConversionRequestDto.setTransactionCurrency("AED");
        CurrencyConversionDto secondConversion = new CurrencyConversionDto();
        secondConversion.setTransactionAmount(paidAmt);

        CoreFundTransferResponseDto coreResponse = new CoreFundTransferResponseDto();
        coreResponse.setMwResponseStatus(MwResponseStatus.S);

        //when

        ReflectionTestUtils.setField(localFundTransferStrategy,"localCurrency", destCurrency);
        ReflectionTestUtils.setField(localFundTransferStrategy,"address", address);
        //ReflectionTestUtils.setField(localFundTransferStrategy,"transactionCode", "015");

        final ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(validationResult);
        when(accountService.getAccountsFromCore(eq(metadata.getPrimaryCif()))).thenReturn(accountsFromCore);
        when(mobCommonService.getPaymentPurposes( eq("LOCAL"), eq(""),eq("I"))).thenReturn(popList);

        when(accountBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(beneficiaryService.getByIdWithoutValidation(eq(metadata.getPrimaryCif()), eq(Long.valueOf(beneId)),any(), any())).thenReturn(beneficiaryDto);

        when(maintenanceService.convertBetweenCurrencies(eq(currencyRequest)))
                .thenReturn(currencyConversionDto);

        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ibanValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(balanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ccTransactionEligibilityValidator.validate(any(), any())).thenReturn(ValidationResult.builder().success(true).build());
        //when(ccBalanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        //when(ccBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(maintenanceService.convertCurrency(eq(currencyConversionRequestDto))).thenReturn(secondConversion);
        LimitValidatorResponse limitValidatorResponse = new LimitValidatorResponse();
        limitValidatorResponse.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate(eq(userDTO), eq("LOCAL"), eq(paidAmt), eq(metadata), any()))
                .thenReturn(limitValidatorResponse);
        //when(dealValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(fundTransferMWService.transfer(fundTransferRequest.capture(), any(),any()))
                .thenReturn(FundTransferResponse.builder().responseDto(coreResponse).limitUsageAmount(paidAmt).limitVersionUuid(limitVersionUuid).build());

        //when(cardService.getCardsFromCore(any(), any())).thenReturn(Collections.emptyList());
        //when(qrDealsService.getQRDealDetails(any(), any())).thenReturn(new QRDealDetails());
        when(minTransactionAmountValidator.validate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(validationResult);

        final FundTransferResponse response = localFundTransferStrategy.execute(requestDTO, metadata, userDTO);
        final FundTransferRequest actualFundTransferRequest = fundTransferRequest.getValue();

        //then
       assertEquals(limitVersionUuid,response.getLimitVersionUuid());
       assertEquals(paidAmt,response.getLimitUsageAmount());
       assertEquals(actualFundTransferRequest.getAmount(),paidAmt);
       assertEquals(actualFundTransferRequest.getProductId(),productId);
       assertEquals(actualFundTransferRequest.getChannel(),channel);
       assertEquals(actualFundTransferRequest.getChannelTraceId(),channelTraceId);
       assertEquals(actualFundTransferRequest.getFromAccount(),fromAcct);
       assertEquals(actualFundTransferRequest.getToAccount(),toAcct);
       assertEquals(actualFundTransferRequest.getPurposeCode(),purposeCode);
       assertEquals(actualFundTransferRequest.getPurposeDesc(),purposeDesc);
       assertEquals(actualFundTransferRequest.getChargeBearer(),chargeBearer);
       assertEquals(actualFundTransferRequest.getFinTxnNo(),finTxnNo);
       assertEquals(actualFundTransferRequest.getSourceCurrency(),srcCurrency);
       assertEquals(actualFundTransferRequest.getSourceBranchCode(),branchCode);
       assertEquals(actualFundTransferRequest.getBeneficiaryFullName(),fullName);
       assertEquals(actualFundTransferRequest.getDestinationCurrency(),destCurrency);
       assertEquals(actualFundTransferRequest.getAwInstBICCode(),swift);
       assertEquals(actualFundTransferRequest.getAwInstName(),bankName);
       assertEquals(actualFundTransferRequest.getBeneficiaryBankCountry(),address);
    }



}
