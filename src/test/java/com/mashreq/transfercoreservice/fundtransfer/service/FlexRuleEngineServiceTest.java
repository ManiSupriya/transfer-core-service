package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountTypeDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMWResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineResponseDTO;
@RunWith(MockitoJUnitRunner.class)
public class FlexRuleEngineServiceTest {

    public static final String DATE_FORMAT = "YYYY-MM-DD";
    @InjectMocks
    FlexRuleEngineService flexRuleEngineService;
    @Mock
    BeneficiaryService beneficiaryService;
    @Mock
    AccountService accountService;
    @Mock
    FlexRuleEngineMWService flexRuleEngineMWService;
    @Mock
    MaintenanceService maintenanceService;
    @Mock
    FlexRuleEngineMetadata metadata;
    @Mock
    FlexRuleEngineRequestDTO request;
    @Mock
    RequestMetaData metaData;
    @Mock
    BeneficiaryDto beneficiaryDto;
    @Mock
    SearchAccountTypeDto searchAccountTypeDto;
    @Mock
    SearchAccountDto searchAccountDto;
    @Mock
    FlexRuleEngineMWResponse flexRuleEngineMWResponse;
    @InjectMocks
    CurrencyConversionDto convertedCurrency;
    @Mock
    BigDecimal val;
    @Before
    public void initTests() {
    	flexRuleEngineService = new FlexRuleEngineService(beneficiaryService, accountService, flexRuleEngineMWService, maintenanceService);
    	metadata = generateFlexRuleEngineMetadata();
    	metaData = generateRequestMetaData();
    	request = generateFlexRuleEngineRequestDTO();
    	searchAccountDto = generateSearchAccountDto();
    	beneficiaryDto = generateBeneficiaryDto();
    	flexRuleEngineMWResponse = generateFlexRuleEngineMWResponse();
    	convertedCurrency = generateCurrencyConversionDto();
    	
    }

    @Test
    public void getRulesOnlyOneAmount() {
    	try {
    	flexRuleEngineService.getRules(metadata, request, metaData);
    	}catch(Throwable throwable) {
    		assertTrue(throwable instanceof GenericException);
    		GenericException genericException = (GenericException) throwable;
    		assertEquals(genericException.getErrorCode(), "TN-6002");
    		
    	}
    }
    @Test
    public void getRulesNullAmount() {
    	request.setTransactionAmount(null);
    	request.setAccountCurrencyAmount(null);
    	try {
    	flexRuleEngineService.getRules(metadata, request, metaData);
    	}catch(Throwable throwable) {
    		assertTrue(throwable instanceof GenericException);
    		GenericException genericException = (GenericException) throwable;
    		assertEquals(genericException.getErrorCode(), "TN-6001");
    		
    	}
    }
    @Test
    public void getRulesAccountCurrencyMatch() {
    	try {
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    		request.setTransactionAmount(null);
    		request.setAccountCurrency("USD");
    	flexRuleEngineService.getRules(metadata, request, metaData);
    	}catch(Throwable throwable) {
    		assertTrue(throwable instanceof GenericException);
    		GenericException genericException = (GenericException) throwable;
    		assertEquals(genericException.getErrorCode(), "TN-4201");
    		
    	}
    }
    @Test
    public void getRulesBeneficiaryCurrencyMatch() {
    	try {
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    	Mockito.when(beneficiaryService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(beneficiaryDto);
    		request.setTransactionAmount(null);
    	flexRuleEngineService.getRules(metadata, request, metaData);
    	}catch(Throwable throwable) {
    		assertTrue(throwable instanceof GenericException);
    		GenericException genericException = (GenericException) throwable;
    		assertEquals(genericException.getErrorCode(), "TN-4003");
    		
    	}
    }

    @Test
    public void getRulesForIndia() {
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    	Mockito.when(beneficiaryService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(beneficiaryDto);
    	Mockito.when(flexRuleEngineMWService.getRules(Mockito.any(), Mockito.any())).thenReturn(flexRuleEngineMWResponse);
    		request.setTransactionAmount(null);
    		request.setTransactionCurrency("AED");
    		beneficiaryDto.setBeneficiaryCountryISO("IN");
    		FlexRuleEngineResponseDTO  flexRuleEngineResponseDTO  = flexRuleEngineService.getRules(metadata, request, metaData);
    		assertNotNull(flexRuleEngineResponseDTO);
    	
    }
    @Test
    public void getRulesForPK() {
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    	Mockito.when(beneficiaryService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(beneficiaryDto);
    	Mockito.when(flexRuleEngineMWService.getRules(Mockito.any(), Mockito.any())).thenReturn(generateFlexRuleEngineMWResponse());
    		request.setAccountCurrencyAmount(null);
    		request.setTransactionCurrency("AED");
    		beneficiaryDto.setBeneficiaryCountryISO("PK");
    		beneficiaryDto.setBeneficiaryCurrency("AED");
    		FlexRuleEngineResponseDTO  flexRuleEngineResponseDTO  = flexRuleEngineService.getRules(metadata, request, metaData);
    	assertNotNull(flexRuleEngineResponseDTO);
    	
    }
    @Test
    public void getRulesForAE() {
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    	Mockito.when(beneficiaryService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(beneficiaryDto);
    	Mockito.when(flexRuleEngineMWService.getRules(Mockito.any(), Mockito.any())).thenReturn(generateFlexRuleEngineMWResponse());
    		request.setAccountCurrencyAmount(null);
    		request.setTransactionCurrency("AED");
    		beneficiaryDto.setBeneficiaryCountryISO("AE");
    		beneficiaryDto.setBeneficiaryCurrency("AED");
    		FlexRuleEngineResponseDTO  flexRuleEngineResponseDTO  = flexRuleEngineService.getRules(metadata, request, metaData);
    		assertNotNull(flexRuleEngineResponseDTO);
    }
    @Test
    public void getChargesAccountCurrency() {
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    	Mockito.when(beneficiaryService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(beneficiaryDto);
    	Mockito.when(flexRuleEngineMWService.getRules(Mockito.any(), Mockito.any())).thenReturn(flexRuleEngineMWResponse);
    	ChargeResponseDTO  chargeResponseDTO  = flexRuleEngineService.getCharges(metadata, request, metaData);
    	assertNotNull(chargeResponseDTO);
    }
    @Test
    public void getChargesChargeCurrency() {
    	request.setAccountCurrency("AED");
    	flexRuleEngineMWResponse.setChargeCurrency("USD");
    	Mockito.when(accountService.getAccountDetailsFromCore(Mockito.any())).thenReturn(searchAccountDto);
    	Mockito.when(beneficiaryService.getById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(beneficiaryDto);
    	Mockito.when(flexRuleEngineMWService.getRules(Mockito.any(), Mockito.any())).thenReturn(flexRuleEngineMWResponse);
    	Mockito.when(maintenanceService.convertBetweenCurrencies(Mockito.any())).thenReturn(convertedCurrency);
    	ChargeResponseDTO  chargeResponseDTO  = flexRuleEngineService.getCharges(metadata, request, metaData);
    	assertNotNull(chargeResponseDTO);
    }
    
    private FlexRuleEngineMetadata generateFlexRuleEngineMetadata(){
    	metadata = FlexRuleEngineMetadata.builder().channelTraceId("testChannelTraceID").cifId("testCif").build();
    	return metadata;
    }
    private FlexRuleEngineRequestDTO generateFlexRuleEngineRequestDTO() {
    	request = FlexRuleEngineRequestDTO.builder().accountCurrency("AED").accountCurrencyAmount(new BigDecimal("200"))
    			 .beneficiaryId(123L).customerAccountNo("123456").transactionAmount(new BigDecimal("100")).
    			 transactionCurrency("USD").build();
    	return request;
    }
    private RequestMetaData generateRequestMetaData() {
    	metaData = new RequestMetaData();
    	metaData.setChannel("MOB");
    	metaData.setChannelTraceId("testChannelTraceID");
    	metaData.setLoginId("12345");
    	return metaData;
    }
    private SearchAccountDto generateSearchAccountDto() {
    	SearchAccountTypeDto searchAccountTypeDto = new SearchAccountTypeDto();
    	searchAccountTypeDto.setAccountType("CONV");
    	searchAccountTypeDto.setSchemaType("MOB");
    searchAccountDto = new SearchAccountDto();
    searchAccountDto.setAccountClosedDate("10-10-2020");
    searchAccountDto.setAccountName("name");
    searchAccountDto.setAccountType(searchAccountTypeDto);
    searchAccountDto.setAvailableBalance("20000");
    searchAccountDto.setBranch("DUBAI");
    searchAccountDto.setChequeBook("checkBook");
    searchAccountDto.setClosed(true);
    searchAccountDto.setCreationDate("10-09-2020");
    searchAccountDto.setCurrency("AED");
    searchAccountDto.setCurrentBalance("10000");
    searchAccountDto.setCustomerCif("123456");
    searchAccountDto.setCustomerName("testCustomer");
    searchAccountDto.setDormant(false);
    searchAccountDto.setFreezeAmount("1000");
    searchAccountDto.setFrozen(true);
    searchAccountDto.setHoldAmount("100");
    searchAccountDto.setJointAccount(false);
    searchAccountDto.setNoCredit(true);
    searchAccountDto.setNoDebit(false);
    searchAccountDto.setNoDebitForCompliance(false);
    searchAccountDto.setNumber("8888888");
    searchAccountDto.setOperatingInstruction("test Account");
    searchAccountDto.setOverDraft("Over Draft");
    searchAccountDto.setOverdraftExpiryDate("10-12-2020");
    searchAccountDto.setOverdraftStartDate("10-09-2020");
    searchAccountDto.setStatus("ACTIVE");
    searchAccountDto.setTempOverDraft("TIme out");
    searchAccountDto.setTotalOverdraft("TIme out");
    searchAccountDto.setUnclearedBalance("2000");
    return searchAccountDto;
    }
    
    private BeneficiaryDto generateBeneficiaryDto() {
    	beneficiaryDto = new BeneficiaryDto();
    	beneficiaryDto.setAccountNumber("123456");
    	beneficiaryDto.setActiveAfter("10-11-2020");
    	beneficiaryDto.setActiveAfterDuration("10-11-2020");
    	beneficiaryDto.setAddressLine1("DUBAI");
    	beneficiaryDto.setAddressLine2("United Arab Emirates");
    	beneficiaryDto.setAddressLine3("DUBAI");
    	beneficiaryDto.setBalanceAmount("2000");
    	beneficiaryDto.setBankAccountType("CONV");
    	beneficiaryDto.setBankBranchName("DUBAI");
    	beneficiaryDto.setBankCity("DUBAI");
    	beneficiaryDto.setBankCode("FZE");
    	beneficiaryDto.setBankCountry("UAE");
    	beneficiaryDto.setBankName("MASHREQ");
    	beneficiaryDto.setBankState("DUBAI");
    	beneficiaryDto.setBeneficiaryAccountType("SAVINGS");
    	beneficiaryDto.setBeneficiaryCity("DUBAI");
    	beneficiaryDto.setBeneficiaryCountryISO("UAE");
    	beneficiaryDto.setBeneficiaryCurrency("AED");
    	beneficiaryDto.setBeneficiaryPostalCode("43001");
    	beneficiaryDto.setBeneficiaryState("DUBAI");
    	beneficiaryDto.setBillRefNo("UID123ER");
    	beneficiaryDto.setCategory("TestCategory");
    	beneficiaryDto.setCreatedDate("10-11-2020");
    	beneficiaryDto.setCurrentBalance("20000");
    	beneficiaryDto.setCreditcardHolderName("testUser");
    	beneficiaryDto.setDocumentNumber("12345ER");
    	beneficiaryDto.setDocumentType("passport");
    	beneficiaryDto.setDueAmount(new BigDecimal("30000"));
    	beneficiaryDto.setFinalName("test");
    	beneficiaryDto.setFullName("test user");
    	beneficiaryDto.setId(1l);
    	beneficiaryDto.setMobileNumber("50516789");
    	beneficiaryDto.setNickname("test");
    	beneficiaryDto.setOutstandingAmount("31000");
    	beneficiaryDto.setRelationship("1234");
    	beneficiaryDto.setRoutingCode("1234");
    	beneficiaryDto.setSalikPinCode("1234");
    	beneficiaryDto.setServiceType("INFT");
    	beneficiaryDto.setServiceTypeCode("INFT");
    	beneficiaryDto.setStatus("ACTIVE");
    	beneficiaryDto.setSwiftCode("swift");
    	return beneficiaryDto;
    }
    
    private FlexRuleEngineMWResponse generateFlexRuleEngineMWResponse() {
    	return FlexRuleEngineMWResponse.builder().accountCurrencyAmount(new BigDecimal("100"))
    			.chargeAmount("200")
    			.chargeCurrency("AED")
    			.exchangeRate(new BigDecimal("2.5"))
    			.productCode("CONV")
    			.transactionAmount(new BigDecimal("2500"))
    			.build();
    	
    }
   private CurrencyConversionDto generateCurrencyConversionDto() {
	   convertedCurrency = new CurrencyConversionDto();
	   convertedCurrency.setAccountCurrencyAmount(new BigDecimal("100"));
	   convertedCurrency.setExchangeRate(new BigDecimal("3.45"));
	   convertedCurrency.setTransactionAmount(new BigDecimal("100"));
    	return convertedCurrency;
    }
}