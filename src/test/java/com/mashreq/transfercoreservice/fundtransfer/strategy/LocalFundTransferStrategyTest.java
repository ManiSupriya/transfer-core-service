package com.mashreq.transfercoreservice.fundtransfer.strategy;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalFundTransferStrategyTest {

    @InjectMocks
    private LocalFundTransferStrategy localFundTransferStrategy;

    @Mock
    private  IBANValidator ibanValidator;

    @Mock
    private  FinTxnNoValidator finTxnNoValidator;

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

    @Captor
    private ArgumentCaptor<FundTransferRequest> fundTransferRequest;


    @Test
    public void test_when_fund_transfer_is_successful_when_source_destination_currency_same() {
        //Given
        BigDecimal limitUsageAmount = new BigDecimal(200);
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

        RequestMetaData metadata =  RequestMetaData.builder().primaryCif(cif).channel(channel).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setBeneficiaryCurrency(srcCurrency);
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setSwiftCode(swift);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setFullName(fullName);
        final List<AccountDetailsDTO> accountsFromCore = Arrays.asList(AccountDetailsDTO.builder()
                .number(fromAcct).currency(srcCurrency).branchCode(branchCode).build());
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));

        //when

        ReflectionTestUtils.setField(localFundTransferStrategy,"localCurrency", srcCurrency);
        ReflectionTestUtils.setField(localFundTransferStrategy,"address", address);
        //ReflectionTestUtils.setField(localFundTransferStrategy,"transactionCode", "015");

        final ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(finTxnNoValidator.validate(requestDTO, metadata)).thenReturn(validationResult);
        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(validationResult);
        when(accountService.getAccountsFromCore(eq(metadata.getPrimaryCif()))).thenReturn(accountsFromCore);
        when(mobCommonService.getPaymentPurposes( eq("local"), eq(""), eq("I"))).thenReturn(popList);

        when(accountBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(beneficiaryService.getById(eq(metadata.getPrimaryCif()), eq(Long.valueOf(beneId)))).thenReturn(beneficiaryDto);
        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ibanValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(balanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate(eq(userDTO), eq("local"), eq(limitUsageAmount)))
                .thenReturn(limitValidatorResultsDto);

        when(fundTransferMWService.transfer(fundTransferRequest.capture(),eq(metadata)))
                .thenReturn(FundTransferResponse.builder().limitUsageAmount(limitUsageAmount).limitVersionUuid(limitVersionUuid).build());

        final FundTransferResponse response = localFundTransferStrategy.execute(requestDTO, metadata, userDTO);
        final FundTransferRequest actualFundTransferRequest = fundTransferRequest.getValue();

        //then
        Assert.assertEquals(limitVersionUuid,response.getLimitVersionUuid());
        Assert.assertEquals(limitUsageAmount,response.getLimitUsageAmount());
        Assert.assertEquals(actualFundTransferRequest.getAmount(),limitUsageAmount);
        Assert.assertEquals(actualFundTransferRequest.getProductId(),productId);
        Assert.assertEquals(actualFundTransferRequest.getChannel(),channel);
        Assert.assertEquals(actualFundTransferRequest.getChannelTraceId(),channelTraceId);
        Assert.assertEquals(actualFundTransferRequest.getFromAccount(),fromAcct);
        Assert.assertEquals(actualFundTransferRequest.getToAccount(),toAcct);
        Assert.assertEquals(actualFundTransferRequest.getPurposeCode(),purposeCode);
        Assert.assertEquals(actualFundTransferRequest.getPurposeDesc(),purposeDesc);
        Assert.assertEquals(actualFundTransferRequest.getChargeBearer(),chargeBearer);
        Assert.assertEquals(actualFundTransferRequest.getFinTxnNo(),finTxnNo);
        Assert.assertEquals(actualFundTransferRequest.getSourceCurrency(),srcCurrency);
        Assert.assertEquals(actualFundTransferRequest.getSourceBranchCode(),branchCode);
        Assert.assertEquals(actualFundTransferRequest.getBeneficiaryFullName(),fullName);
        Assert.assertEquals(actualFundTransferRequest.getDestinationCurrency(),srcCurrency);
        Assert.assertEquals(actualFundTransferRequest.getAwInstBICCode(),swift);
        Assert.assertEquals(actualFundTransferRequest.getAwInstName(),bankName);
        Assert.assertEquals(actualFundTransferRequest.getBeneficiaryAddressTwo(),address);
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

        RequestMetaData metadata =  RequestMetaData.builder().primaryCif(cif).channel(channel).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setBeneficiaryCurrency(srcCurrency);
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setSwiftCode(swift);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setBeneficiaryCurrency(destCurrency);
        beneficiaryDto.setFullName(fullName);
        final List<AccountDetailsDTO> accountsFromCore = Arrays.asList(AccountDetailsDTO.builder()
                .number(fromAcct).currency(srcCurrency).branchCode(branchCode).build());
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));


        CoreCurrencyConversionRequestDto currencyRequest = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(fromAcct)
                .accountCurrency(srcCurrency)
                .transactionCurrency(destCurrency)
                .transactionAmount(paidAmt).build();
        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(paidAmtInSrcCurrency);

        CoreCurrencyConversionRequestDto currencyConversionRequestDto = CoreCurrencyConversionRequestDto.builder()
                .accountNumber(fromAcct)
                .accountCurrency(srcCurrency)
                .accountCurrencyAmount(paidAmtInSrcCurrency)
                .dealNumber(null)
                .transactionCurrency("AED")
                .build();
        CurrencyConversionDto secondConversion = new CurrencyConversionDto();
        secondConversion.setTransactionAmount(paidAmt);
        //when

        ReflectionTestUtils.setField(localFundTransferStrategy,"localCurrency", destCurrency);
        ReflectionTestUtils.setField(localFundTransferStrategy,"address", address);
        //ReflectionTestUtils.setField(localFundTransferStrategy,"transactionCode", "015");

        final ValidationResult validationResult = ValidationResult.builder().success(true).build();
        when(finTxnNoValidator.validate(requestDTO, metadata)).thenReturn(validationResult);
        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(validationResult);
        when(accountService.getAccountsFromCore(eq(metadata.getPrimaryCif()))).thenReturn(accountsFromCore);
        when(mobCommonService.getPaymentPurposes( eq("local"), eq(""),eq("I"))).thenReturn(popList);

        when(accountBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(beneficiaryService.getById(eq(metadata.getPrimaryCif()), eq(Long.valueOf(beneId)))).thenReturn(beneficiaryDto);

        when(maintenanceService.convertBetweenCurrencies(eq(currencyRequest)))
                .thenReturn(currencyConversionDto);

        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(ibanValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());
        when(balanceValidator.validate(eq(requestDTO), eq(metadata), any())).thenReturn(ValidationResult.builder().success(true).build());

        when(maintenanceService.convertCurrency(eq(currencyConversionRequestDto))).thenReturn(secondConversion);
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate(eq(userDTO), eq("local"), eq(paidAmt)))
                .thenReturn(limitValidatorResultsDto);

        when(fundTransferMWService.transfer(fundTransferRequest.capture(),eq(metadata)))
                .thenReturn(FundTransferResponse.builder().limitUsageAmount(paidAmt).limitVersionUuid(limitVersionUuid).build());

        final FundTransferResponse response = localFundTransferStrategy.execute(requestDTO, metadata, userDTO);
        final FundTransferRequest actualFundTransferRequest = fundTransferRequest.getValue();

        //then
        Assert.assertEquals(limitVersionUuid,response.getLimitVersionUuid());
        Assert.assertEquals(paidAmt,response.getLimitUsageAmount());
        Assert.assertEquals(actualFundTransferRequest.getAmount(),paidAmt);
        Assert.assertEquals(actualFundTransferRequest.getProductId(),productId);
        Assert.assertEquals(actualFundTransferRequest.getChannel(),channel);
        Assert.assertEquals(actualFundTransferRequest.getChannelTraceId(),channelTraceId);
        Assert.assertEquals(actualFundTransferRequest.getFromAccount(),fromAcct);
        Assert.assertEquals(actualFundTransferRequest.getToAccount(),toAcct);
        Assert.assertEquals(actualFundTransferRequest.getPurposeCode(),purposeCode);
        Assert.assertEquals(actualFundTransferRequest.getPurposeDesc(),purposeDesc);
        Assert.assertEquals(actualFundTransferRequest.getChargeBearer(),chargeBearer);
        Assert.assertEquals(actualFundTransferRequest.getFinTxnNo(),finTxnNo);
        Assert.assertEquals(actualFundTransferRequest.getSourceCurrency(),srcCurrency);
        Assert.assertEquals(actualFundTransferRequest.getSourceBranchCode(),branchCode);
        Assert.assertEquals(actualFundTransferRequest.getBeneficiaryFullName(),fullName);
        Assert.assertEquals(actualFundTransferRequest.getDestinationCurrency(),destCurrency);
        Assert.assertEquals(actualFundTransferRequest.getAwInstBICCode(),swift);
        Assert.assertEquals(actualFundTransferRequest.getAwInstName(),bankName);
        Assert.assertEquals(actualFundTransferRequest.getBeneficiaryAddressTwo(),address);
    }



}
