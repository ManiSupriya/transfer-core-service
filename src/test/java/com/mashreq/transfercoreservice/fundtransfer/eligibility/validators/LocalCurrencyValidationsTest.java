package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.validators.LocalCurrencyValidations;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

/**
 * 
 * @author JasarJa
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalCurrencyValidationsTest {
	
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	
	@Mock
    private MobCommonClient mobCommonClient;

	private LocalCurrencyValidations localCurrencyValidations;
	
	private RequestMetaData metadata = RequestMetaData.builder().country("EG").build();
	
	@Before
	public void init() {
		localCurrencyValidations = new LocalCurrencyValidations("EGP", auditEventPublisher, mobCommonClient);
	}
	
	@Test
    public void validate_invalidinput() { 
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(null)
				.validationContext(null)
				.serviceType(ServiceType.WAMA.getName())
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());
	}

    //START - WYMA transfer test cases
    /**
     * Test case - WYMATC1
     * Egypt WYMA transfer. 
     * From account - EGP
     * Transaction - EGP 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WYMATC1() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        		
        String transactionCurrency = "EGP";
        String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC2
     * Egypt WYMA transfer. 
     * From account - EGP
     * Transaction - USD 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since transaction currency is not EGP
     */
    @Test
    public void validate_WYMATC2() { 
    	
    	String transactionCurrency = "USD";
        String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC3
     * Egypt WYMA transfer. 
     * From account - EGP
     * Transaction - USD 
     * To account - USD 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since destination account currency is not EGP
     */
    @Test
    public void validate_WYMATC3() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("USD");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC4
     * Egypt WYMA transfer. 
     * From account - USD
     * Transaction - EGP 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WYMATC4() { 
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("USD");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();
        
        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC5
     * Egypt WYMA transfer.  
     * From account - INR
     * Transaction - INR 
     * To account - USD 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation failed since destination account currency is not local or source account currency
     */
    @Test
    public void validate_WYMATC5() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("INR");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("USD");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC6
     * Egypt WYMA transfer.  
     * From account - INR
     * Transaction - INR 
     * To account - INR 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WYMATC6() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("INR");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("INR");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC7
     * Egypt WYMA transfer.  
     * From account - INR
     * Transaction - INR 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WYMATC7() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("INR");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC8
     * Egypt WYMA transfer.  
     * From account - INR
     * Transaction - USD 
     * To account - EGP
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation failed since transaction currency is not local or source account currency
     */
    @Test
    public void validate_WYMATC8() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("INR");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC9
     * Egypt WYMA transfer.  
     * From account - INR
     * Transaction - EGP 
     * To account - USD
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation failed since destination account currency is not local or source account currency
     */
    @Test
    public void validate_WYMATC9() { 
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.WYMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("INR");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("USD");
        
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    //START - WAMA transfer test cases
    /**
     * Test case - WAMATC1
     * Egypt WAMA transfer. 
     * From account - EGP
     * Transaction - EGP 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC1() { 
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.WAMA.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        mockValidationContext.add("credit-account-details", toAccount);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC2
     * Egypt WYMA transfer. 
     * From account - EGP
     * Transaction - USD 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since transaction currency is not EGP
     */
    @Test
    public void validate_WAMATC2() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("EGP");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC3
     * Egypt WAMA transfer. 
     * From account - EGP
     * Transaction - EGP 
     * To account - USD 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since destination account currency is not EGP
     */
    @Test
    public void validate_WAMATC3() {
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("USD");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC4
     * Egypt WAMA transfer. 
     * From account - USD
     * Transaction - EGP 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC4() { 
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("USD");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("EGP");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC5
     * Egypt WAMA transfer. 
     * From account - INR
     * Transaction - INR 
     * To account - USD 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation failed since destination account currency is not EGP
     */
    @Test
    public void validate_WAMATC5() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("INR");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("USD");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC6
     * Egypt WAMA transfer. 
     * From account - INR
     * Transaction - INR 
     * To account - USD 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation failed since destination account currency is not EGP
     */
    @Test
    public void validate_WAMATC6() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("INR");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("EGP");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC7
     * Egypt WAMA transfer. 
     * From account - INR
     * Transaction - INR 
     * To account - INR 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC7() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("INR");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("INR");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC8
     * Egypt WAMA transfer. 
     * From account - INR
     * Transaction - INR 
     * To account - EGP 
     * 
     * Validation
     * Transaction currency - Either EGP or source account currency
     * Destination account currency - Either EGP or source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC8() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.WAMA.getName();

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("INR");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("EGP");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);
		
		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    //START - LOCAL transfer test cases
    /**
     * Test case - LOCALTC1
     * Egypt LOCAL transfer. 
     * From account - EGP
     * Transaction - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_LOCALTC1() { 
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.LOCAL.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - LOCALTC2
     * Egypt LOCAL transfer. 
     * From account - EGP
     * Transaction - USD 
     * 
     * Validation
     * Transaction currency should be EGP
     * 
     * Expected: Validation failed since transaction currency is not EGP
     */
    @Test
    public void validate_LOCALTC2() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.LOCAL.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - LOCALTC3
     * Egypt LOCAL transfer. 
     * From account - USD
     * Transaction - USD 
     * 
     * Validation
     * Transaction currency should be same as source account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_LOCALTC3() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.LOCAL.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("USD");
        ValidationContext mockValidationContext = new ValidationContext();

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - LOCALTC4
     * Egypt LOCAL transfer. 
     * From account - USD
     * Transaction - INR 
     * 
     * Validation
     * Transaction currency should be same as source account currency
     * 
     * Expected: Validation failed since transaction currency is not EGP
     */
    @Test
    public void validate_LOCALTC4() { 
    	
    	String transactionCurrency = "INR";
    	String serviceType = ServiceType.LOCAL.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("USD");
        ValidationContext mockValidationContext = new ValidationContext();

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();
        
        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    //START - INFT transfer test cases
    /**
     * Test case - INFTTC1
     * Egypt INFT transfer. 
     * From account - USD
     * Transaction - USD 
     * 
     * Validation
     * Source account cannot be EGP
     * Transaction currency cannot be EGP
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_INFTTC1() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.INFT.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("USD");
        
		ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - INFTTC2
     * Egypt INFT transfer. 
     * From account - USD
     * Transaction - EGP 
     * 
     * Validation
     * Source account cannot be EGP
     * Transaction currency cannot be EGP
     * 
     * Expected: Validation failed since transaction currency is EGP
     */
    @Test
    public void validate_INFTTC2() { 
    	
    	String transactionCurrency = "EGP";
    	String serviceType = ServiceType.INFT.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("USD");
        
		ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - INFTTC3
     * Egypt INFT transfer. 
     * From account - EGP
     * Transaction - USD 
     * 
     * Validation
     * Source account cannot be EGP
     * Transaction currency cannot be EGP
     * 
     * Expected: Validation failed since transaction currency is EGP
     */
    @Test
    public void validate_INFTTC3() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.INFT.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
        
		ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - INFTTC4
     * Egypt INFT transfer. 
     * From account - USD
     * Transaction - INR 
     * 
     * Validation: Transaction currency should be same as source account currency
     * 
     * Expected: Validation failed since transaction currency is not same as source account currency
     */
    @Test
    public void validate_INFTTC4() { 
    	
    	String transactionCurrency = "USD";
    	String serviceType = ServiceType.INFT.getName();

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("INR");
        
		ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", accountDetailsDTO);
        
        LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(transactionCurrency)
				.validationContext(mockValidationContext)
				.serviceType(serviceType)
				.build();

        final ValidationResult result = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
        assertEquals(false, result.isSuccess());

    }
}
