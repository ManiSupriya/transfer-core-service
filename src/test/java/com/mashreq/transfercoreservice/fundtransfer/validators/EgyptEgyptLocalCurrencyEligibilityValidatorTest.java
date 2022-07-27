package com.mashreq.transfercoreservice.fundtransfer.validators;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;

/**
 * 
 * @author JasarJa
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EgyptEgyptLocalCurrencyEligibilityValidatorTest {

	@Mock
    private AsyncUserEventPublisher auditEventPublisher;

    private EgyptLocalCurrencyValidator egyptLocalCurrencyValidator;

    
    private RequestMetaData metadata = RequestMetaData.builder().country("EG").build();
    
    @Before
    public void init() {
    	egyptLocalCurrencyValidator = new EgyptLocalCurrencyValidator( auditEventPublisher, new LocalCurrencyValidations("EGP", auditEventPublisher));
    	egyptLocalCurrencyValidator.init();
    }
    
    //START - WYMA transfer test cases
    /**
     * Test case - WYMATC1
     * Egypt WYMA transfer. 
     * From account - EGP
     * To account - EGP 
     * Transaction - EGP 
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
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setTxnCurrency("EGP");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC2
     * Egypt WYMA transfer. 
     * From account - EGP
     * To account - EGP 
     * Transaction - USD 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since transaction currency is not EGP
     */
    @Test
    public void validate_WYMATC2() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setTxnCurrency("USD");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC3
     * Egypt WYMA transfer. 
     * From account - EGP
     * To account - USD 
     * Transaction - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since destination account currency is not EGP
     */
    @Test
    public void validate_WYMATC3() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setTxnCurrency("USD");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("USD");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC4
     * Egypt WYMA transfer. 
     * From account - USD
     * To account - EGP 
     * Transaction - EGP 
     * 
     * Validation
     * Transaction currency - Either source or destination account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WYMATC4() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("USD");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setTxnCurrency("EGP");
        
        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);
        
        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WYMATC5
     * Egypt WYMA transfer.  
     * From account - INR
     * To account - USD 
     * Transaction - INR 
     * 
     * Validation
     * Transaction currency - Either source or destination account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WYMATC5() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("INR");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setTxnCurrency("INR");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("USD");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(true, result.isSuccess());

    }
    
    
    //START - WAMA transfer test cases
    /**
     * Test case - WAMATC1
     * Egypt WAMA transfer. 
     * From account - EGP
     * To account - EGP 
     * Transaction - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC1() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
        		
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setTxnCurrency("EGP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("EGP");
        
        ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC2
     * Egypt WYMA transfer. 
     * From account - EGP
     * To account - EGP 
     * Transaction - USD 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since transaction currency is not EGP
     */
    @Test
    public void validate_WAMATC2() { 

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
		
		FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
		requestDTO.setServiceType(ServiceType.WAMA.getName());
		requestDTO.setTxnCurrency("USD");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("EGP");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC3
     * Egypt WAMA transfer. 
     * From account - EGP
     * To account - USD 
     * Transaction - EGP 
     * 
     * Validation
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Expected: Validation failed since destination account currency is not EGP
     */
    @Test
    public void validate_WAMATC3() {

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
		
		FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
		requestDTO.setServiceType(ServiceType.WAMA.getName());
		requestDTO.setTxnCurrency("EGP");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("USD");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(false, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC4
     * Egypt WAMA transfer. 
     * From account - USD
     * To account - EGP 
     * Transaction - EGP 
     * 
     * Validation
     * Transaction currency - Either source or destination account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC4() { 

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("USD");
		
		FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
		requestDTO.setServiceType(ServiceType.WAMA.getName());
		requestDTO.setTxnCurrency("EGP");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("EGP");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(true, result.isSuccess());

    }
    
    /**
     * Test case - WAMATC5
     * Egypt WAMA transfer. 
     * From account - INR
     * To account - USD 
     * Transaction - INR 
     * 
     * Validation
     * Transaction currency - Either source or destination account currency
     * 
     * Expected: Validation success
     */
    @Test
    public void validate_WAMATC5() { 

    	AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("INR");
		
		FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
		requestDTO.setServiceType(ServiceType.WAMA.getName());
		requestDTO.setTxnCurrency("INR");
		
		BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
		beneficiaryDto.setAccountNumber("019022073766");
		beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());
		
		SearchAccountDto toAccount = new SearchAccountDto();
		toAccount.setCurrency("USD");
		
		ValidationContext mockValidationContext = new ValidationContext();
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("beneficiary-dto", beneficiaryDto);
		mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
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

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setTxnCurrency("EGP");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
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

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setTxnCurrency("USD");

        AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
        toAccountDetailsDTO.setNumber("019010073766");
        toAccountDetailsDTO.setCurrency("EGP");
        
        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("to-account", toAccountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
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

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("USD");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.INFT.getName());
        requestDTO.setTxnCurrency("USD");
        
        mockValidationContext.add("from-account", accountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
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

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("USD");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.INFT.getName());
        requestDTO.setTxnCurrency("EGP");
        
        mockValidationContext.add("from-account", accountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
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

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.INFT.getName());
        requestDTO.setTxnCurrency("USD");
        
        mockValidationContext.add("from-account", accountDetailsDTO);

        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(false, result.isSuccess());

    }
}
