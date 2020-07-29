
package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerPhones;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.mapper.QuickRemitPakistanRequestMapper;
import com.mashreq.transfercoreservice.fundtransfer.service.QuickRemitFundTransferMWService;
import com.mashreq.transfercoreservice.fundtransfer.strategy.utils.CustomerDetailsUtils;
import com.mashreq.transfercoreservice.fundtransfer.validators.*;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuickRemitPakistanStrategyTest {

    @InjectMocks
    private QuickRemitPakistanStrategy quickRemitPakistanStrategy;

    @Mock
    private AccountService accountService;

    @Mock
    private MobCommonService mobCommonService;

    @Mock
    private MaintenanceService maintenanceService;

    @Mock
    private QuickRemitFundTransferMWService quickRemitFundTransferMWService;

    @Mock
    private FinTxnNoValidator finTxnNoValidator;

    @Mock
    private AccountBelongsToCifValidator accountBelongsToCifValidator;

    @Mock
    private PaymentPurposeValidator paymentPurposeValidator;

    @Mock
    private BeneficiaryValidator beneficiaryValidator;

    @Mock
    private BalanceValidator balanceValidator;

    @Mock
    private LimitValidator limitValidator;

    @Mock
    private QuickRemitPakistanRequestMapper quickRemitPakistanRequestMapper;

    @Test
    public void test_when_source_account_currency_is_aed() {
        //given
        BigDecimal limitUsageAmount = new BigDecimal("23.01");
        BigDecimal txnAmount = new BigDecimal(1000);
        String limitVersionUuid = "uuid1234";
        String fromAcct = "019010050532";
        String toAcct = "019010050544";
        String channel = "MOB";
        String channelTraceId = "traceId123";
        String cif = "12345";
        String beneId = "121";
        String srcCurrency = "AED";
        String destCurrency = "PKR";
        String productId = "TRTPTPK";
        String purposeDesc = "Salary";
        String purposeCode = "SAL";
        String chargeBearer = "O";
        String finTxnNo = "fin123";
        String branchCode = "083";
        String fullName = "Deepa Shivakumar";
        String bankName = "CITI BANK MG ROAD";
        String address = "UNITED ARAB EMIRATES";
        String productCode = "DRFC";
        BigDecimal exchangeRate = new BigDecimal("3.01");
        BigDecimal amountInSrcCurrency = new BigDecimal("23.01");

        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount(toAcct);
        requestDTO.setFromAccount(fromAcct);
        requestDTO.setPurposeDesc(purposeDesc);
        requestDTO.setChargeBearer(chargeBearer);
        requestDTO.setPurposeCode(purposeCode);
        requestDTO.setFinTxnNo(finTxnNo);
        requestDTO.setAmount(txnAmount);
        requestDTO.setServiceType(ServiceType.QUICK_REMIT.getName());
        requestDTO.setBeneficiaryId(beneId);
        requestDTO.setProductCode(productCode);
        List<CustomerPhones> customerPhonesList = new ArrayList<>();
        CustomerPhones customerPhones = new CustomerPhones();
        customerPhones.setMobNumber("1234567899");
        customerPhones.setPhoneNumberType("P");
        CustomerPhones customerPhones1 = new CustomerPhones();
        customerPhones1.setMobNumber("1234567898");
        customerPhones1.setPhoneNumberType("O");
        customerPhonesList.add(customerPhones1);
        customerPhonesList.add(customerPhones);
       
        CustomerDetailsDto customerDetails = new CustomerDetailsDto();
        customerDetails.setCifBranch("078");
        customerDetails.setNationality("UK");
        customerDetails.setPhones(customerPhonesList);
        customerDetails.setUniqueIDName("PASSPORT");
        customerDetails.setUniqueIDValue("33333333");


        RequestMetaData metadata =  RequestMetaData.builder().primaryCif(cif)
                .channel(channel).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();
        final ValidationResult validationResult = ValidationResult.builder().success(true).build();

        final AccountDetailsDTO sourceAccountDetailsDTO = new AccountDetailsDTO();
        sourceAccountDetailsDTO.setNumber(fromAcct);
        sourceAccountDetailsDTO.setCurrency(srcCurrency);
        sourceAccountDetailsDTO.setBranchCode(branchCode);
        sourceAccountDetailsDTO.setCustomerName("Deepa S");
        final List<AccountDetailsDTO> accountsFromCore = Arrays.asList(sourceAccountDetailsDTO);

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setBeneficiaryCurrency(destCurrency);
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setFullName(fullName);
        beneficiaryDto.setFinalName(fullName);
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));
        CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        		currencyRequest.setAccountNumber(fromAcct);
        		currencyRequest.setAccountCurrency(srcCurrency);
        		currencyRequest.setTransactionCurrency(destCurrency);
        		currencyRequest.setProductCode(productCode);
        		currencyRequest.setTransactionAmount(txnAmount);

        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(amountInSrcCurrency);
        currencyConversionDto.setExchangeRate(exchangeRate);

        ValidationContext validationContext = new ValidationContext();
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("src-currency-iso", "784");
        validationContext.add("src-country-iso", "AE");
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);
        validationContext.add("validate-from-account", Boolean.TRUE);
        validationContext.add("purposes", popList);
        validationContext.add("transfer-amount-in-source-currency", currencyConversionDto.getAccountCurrencyAmount());


        QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .serviceCode("TRTPTPK")
                .amountDESTCurrency(txnAmount)
                .amountSRCCurrency(amountInSrcCurrency)
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                .beneficiaryCountry(beneficiaryDto.getBankCountry())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .beneficiaryName(StringUtils.defaultIfBlank(beneficiaryDto.getFinalName(), beneficiaryDto.getFullName()))
                .channelTraceId(channelTraceId)
                .destCountry(beneficiaryDto.getBeneficiaryCountryISO())
                .srcISOCurrency("784")
                .destCurrency(destCurrency)
                .destISOCurrency("586")
                .exchangeRate(exchangeRate)
                .finTxnNo(channelTraceId)
                .originatingCountry("AE")
                .reasonCode(purposeCode)
                .reasonText(purposeDesc)
                .senderBankAccount(sourceAccountDetailsDTO.getNumber())
                //todo : change to digital user country
                .senderCountryISOCode(customerDetails.getNationality())
                .senderBankBranch(customerDetails.getCifBranch())
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetails))
                .senderName(sourceAccountDetailsDTO.getCustomerName())
                .srcCurrency(srcCurrency)
                .transactionAmount(txnAmount.toString())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .distributionType("Direct Credit")
                .transferType("FT")
                .beneficiaryMobileNo(beneficiaryDto.getMobileNumber())
                .beneficiaryBankCode(beneficiaryDto.getBankCode())
                .beneficiaryIdType(beneficiaryDto.getDocumentType())
                .beneficiaryIdNo(beneficiaryDto.getDocumentNumber())
                .build();
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);
        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto)
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(limitVersionUuid)
                .build();
        //when
        when(finTxnNoValidator.validate(requestDTO, metadata))
                .thenReturn(validationResult);
        when(mobCommonService.getCustomerDetails(Mockito.eq(metadata.getPrimaryCif())))
                .thenReturn(customerDetails);
        when(accountService.getAccountsFromCore(Mockito.eq(metadata.getPrimaryCif())))
                .thenReturn(accountsFromCore);
        when(accountBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any()))
                .thenReturn(ValidationResult.builder().success(true).build());
        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any()))
                .thenReturn(ValidationResult.builder().success(true).build());

        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any()))
                .thenReturn(validationResult);

        when(maintenanceService.convertBetweenCurrencies(any()))
                .thenReturn(currencyConversionDto);
        when(balanceValidator.validate(eq(requestDTO),eq(metadata),any())).thenReturn(validationResult);
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate(eq(userDTO), eq("QRT"), eq(limitUsageAmount), eq(metadata)))
                .thenReturn(limitValidatorResultsDto);
        when(quickRemitPakistanRequestMapper.map(eq(channelTraceId),eq(requestDTO), eq(sourceAccountDetailsDTO),
                eq(beneficiaryDto), eq(currencyConversionDto.getAccountCurrencyAmount()),
                eq(currencyConversionDto.getExchangeRate()), eq(validationContext), eq(customerDetails))).thenReturn(quickRemitFundTransferRequest);
        when(quickRemitFundTransferMWService.transfer(quickRemitFundTransferRequest,metadata)).thenReturn(response);

        FundTransferResponse result = quickRemitPakistanStrategy.execute(requestDTO, metadata, userDTO, validationContext);

        //then
        Assert.assertEquals(result.getResponseDto(),response.getResponseDto());

    }


    @Test
    public void test_when_source_account_currency_is_usd() {
        //given
        BigDecimal limitUsageAmount = new BigDecimal("22.81");
        BigDecimal txnAmount = new BigDecimal(1000);
        String limitVersionUuid = "uuid1234";
        String fromAcct = "019010050532";
        String toAcct = "019010050544";
        String channel = "MOB";
        String channelTraceId = "traceId123";
        String cif = "12345";
        String beneId = "121";
        String srcCurrency = "USD";
        String destCurrency = "PKR";
        String productId = "TRTPTPK";
        String purposeDesc = "Salary";
        String purposeCode = "SAL";
        String chargeBearer = "O";
        String finTxnNo = "fin123";
        String branchCode = "083";
        String fullName = "Deepa Shivakumar";
        String bankName = "CITI BANK MG ROAD";
        String address = "UNITED ARAB EMIRATES";
        String productCode = "DRFC";
        BigDecimal exchangeRate = new BigDecimal("3.01");
        BigDecimal amountInSrcCurrency = new BigDecimal("6.21");

        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount(toAcct);
        requestDTO.setFromAccount(fromAcct);
        requestDTO.setPurposeDesc(purposeDesc);
        requestDTO.setChargeBearer(chargeBearer);
        requestDTO.setPurposeCode(purposeCode);
        requestDTO.setFinTxnNo(finTxnNo);
        requestDTO.setAmount(txnAmount);
        requestDTO.setServiceType(ServiceType.QUICK_REMIT.getName());
        requestDTO.setBeneficiaryId(beneId);
        requestDTO.setProductCode(productCode);
        
        List<CustomerPhones> customerPhonesList = new ArrayList<>();
        CustomerPhones customerPhones = new CustomerPhones();
        customerPhones.setMobNumber("1234567899");
        customerPhones.setPhoneNumberType("P");
        CustomerPhones customerPhones1 = new CustomerPhones();
        customerPhones1.setMobNumber("1234567898");
        customerPhones1.setPhoneNumberType("O");
        customerPhonesList.add(customerPhones1);
        customerPhonesList.add(customerPhones);
       
        CustomerDetailsDto customerDetailsDto = new CustomerDetailsDto();
        customerDetailsDto.setCifBranch("078");
        customerDetailsDto.setNationality("UK");
        customerDetailsDto.setPhones(customerPhonesList);
        customerDetailsDto.setUniqueIDName("PASSPORT");
        customerDetailsDto.setUniqueIDValue("33333333");

        RequestMetaData metadata =  RequestMetaData.builder().primaryCif(cif)
                .channel(channel).channelTraceId(channelTraceId).build();
        UserDTO userDTO = new UserDTO();
        final ValidationResult validationResult = ValidationResult.builder().success(true).build();

        final AccountDetailsDTO sourceAccountDetailsDTO = new AccountDetailsDTO();
        sourceAccountDetailsDTO.setNumber(fromAcct);
        sourceAccountDetailsDTO.setCurrency(srcCurrency);
        sourceAccountDetailsDTO.setBranchCode(branchCode);
        sourceAccountDetailsDTO.setCustomerName("Deepa S");
        final List<AccountDetailsDTO> accountsFromCore = Arrays.asList(sourceAccountDetailsDTO);

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setBeneficiaryCurrency(destCurrency);
        beneficiaryDto.setAccountNumber(toAcct);
        beneficiaryDto.setBankName(bankName);
        beneficiaryDto.setFullName(fullName);
        beneficiaryDto.setFinalName(fullName);
        final Set<MoneyTransferPurposeDto> popList = new HashSet(Arrays.asList(MoneyTransferPurposeDto.class));


        CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
        currencyRequest.setAccountNumber(fromAcct);
        currencyRequest.setAccountCurrency(srcCurrency);
        currencyRequest.setTransactionCurrency(destCurrency);
        currencyRequest.setProductCode(productCode);
        currencyRequest.setTransactionAmount(txnAmount);

        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(amountInSrcCurrency);
        currencyConversionDto.setExchangeRate(exchangeRate);

        CoreCurrencyConversionRequestDto currencyConversionRequestDtoForLimit = new CoreCurrencyConversionRequestDto();
        currencyConversionRequestDtoForLimit.setAccountNumber(fromAcct);
        currencyConversionRequestDtoForLimit.setAccountCurrency(srcCurrency);
        currencyConversionRequestDtoForLimit.setAccountCurrencyAmount(amountInSrcCurrency);
        currencyConversionRequestDtoForLimit.setTransactionCurrency("AED");

        CurrencyConversionDto currencyConversionDtoInAed = new CurrencyConversionDto();
        currencyConversionDtoInAed.setTransactionAmount(limitUsageAmount);
        currencyConversionDtoInAed.setExchangeRate(exchangeRate);

        ValidationContext validationContext = new ValidationContext();
        validationContext.add("beneficiary-dto", beneficiaryDto);
        validationContext.add("src-currency-iso", "784");
        validationContext.add("src-country-iso", "AE");
        validationContext.add("account-details", accountsFromCore);
        validationContext.add("to-account-currency", beneficiaryDto.getBeneficiaryCurrency());
        validationContext.add("from-account", sourceAccountDetailsDTO);
        validationContext.add("validate-from-account", Boolean.TRUE);
        validationContext.add("purposes", popList);
        validationContext.add("transfer-amount-in-source-currency", currencyConversionDto.getAccountCurrencyAmount());


        QuickRemitFundTransferRequest quickRemitFundTransferRequest = QuickRemitFundTransferRequest.builder()
                .serviceCode("TRTPTPK")
                .amountDESTCurrency(txnAmount)
                .amountSRCCurrency(amountInSrcCurrency)
                .beneficiaryAccountNo(beneficiaryDto.getAccountNumber())
                .beneficiaryBankName(beneficiaryDto.getBankName())
                .beneficiaryCountry(beneficiaryDto.getBankCountry())
                .beneficiaryFullName(beneficiaryDto.getFullName())
                .beneficiaryName(StringUtils.defaultIfBlank(beneficiaryDto.getFinalName(), beneficiaryDto.getFullName()))
                .channelTraceId(channelTraceId)
                .destCountry(beneficiaryDto.getBeneficiaryCountryISO())
                .srcISOCurrency("784")
                .destCurrency(destCurrency)
                .destISOCurrency("586")
                .exchangeRate(exchangeRate)
                .finTxnNo(channelTraceId)
                .originatingCountry("AE")
                .reasonCode(purposeCode)
                .reasonText(purposeDesc)
                .senderBankAccount(sourceAccountDetailsDTO.getNumber())
                //todo : change to digital user country
                .senderCountryISOCode(customerDetailsDto.getNationality())
                .senderBankBranch(customerDetailsDto.getCifBranch())
                .senderMobileNo(CustomerDetailsUtils.getMobileNumber(customerDetailsDto))
                .senderName(sourceAccountDetailsDTO.getCustomerName())
                .srcCurrency(srcCurrency)
                .transactionAmount(txnAmount.toString())
                .transactionCurrency(beneficiaryDto.getBeneficiaryCurrency())
                .distributionType("Direct Credit")
                .transferType("FT")
                .beneficiaryMobileNo(beneficiaryDto.getMobileNumber())
                .beneficiaryBankCode(beneficiaryDto.getBankCode())
                .beneficiaryIdType(beneficiaryDto.getDocumentType())
                .beneficiaryIdNo(beneficiaryDto.getDocumentNumber())
                .build();
        
        CoreFundTransferResponseDto coreFundTransferResponseDto = new CoreFundTransferResponseDto();
        coreFundTransferResponseDto.setMwResponseStatus(MwResponseStatus.S);

        FundTransferResponse response = FundTransferResponse.builder()
                .responseDto(coreFundTransferResponseDto)
                .limitUsageAmount(limitUsageAmount)
                .limitVersionUuid(limitVersionUuid)
                .build();
        //when
        when(finTxnNoValidator.validate(requestDTO, metadata))
                .thenReturn(validationResult);
        when(mobCommonService.getCustomerDetails(Mockito.eq(metadata.getPrimaryCif())))
                .thenReturn(customerDetailsDto);
        when(accountService.getAccountsFromCore(Mockito.eq(metadata.getPrimaryCif())))
                .thenReturn(accountsFromCore);
        when(accountBelongsToCifValidator.validate(eq(requestDTO), eq(metadata), any()))
                .thenReturn(ValidationResult.builder().success(true).build());
        when(beneficiaryValidator.validate(eq(requestDTO), eq(metadata), any()))
                .thenReturn(ValidationResult.builder().success(true).build());
        when(paymentPurposeValidator.validate(eq(requestDTO), eq(metadata), any()))
                .thenReturn(validationResult);

        when(maintenanceService.convertBetweenCurrencies(eq(currencyRequest)))
                .thenReturn(currencyConversionDto);
        when(balanceValidator.validate(eq(requestDTO),eq(metadata),any())).thenReturn(validationResult);

        when(maintenanceService.convertCurrency(eq(currencyConversionRequestDtoForLimit)))
                .thenReturn(currencyConversionDtoInAed);
        LimitValidatorResultsDto limitValidatorResultsDto = new LimitValidatorResultsDto();
        limitValidatorResultsDto.setLimitVersionUuid(limitVersionUuid);
        when(limitValidator.validate(eq(userDTO), eq("QRT"), eq(limitUsageAmount), eq(metadata)))
                .thenReturn(limitValidatorResultsDto);
        when(quickRemitPakistanRequestMapper.map(eq(channelTraceId),eq(requestDTO), eq(sourceAccountDetailsDTO),
                eq(beneficiaryDto), eq(currencyConversionDto.getAccountCurrencyAmount()),
                eq(currencyConversionDto.getExchangeRate()), eq(validationContext), eq(customerDetailsDto))).thenReturn(quickRemitFundTransferRequest);
        when(quickRemitFundTransferMWService.transfer(quickRemitFundTransferRequest,metadata)).thenReturn(response);

        FundTransferResponse result = quickRemitPakistanStrategy.execute(requestDTO, metadata, userDTO, validationContext);

        //then
        Assert.assertEquals(result.getResponseDto(),response.getResponseDto());

    }


}

