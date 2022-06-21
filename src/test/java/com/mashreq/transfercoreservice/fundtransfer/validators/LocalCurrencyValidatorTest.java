package com.mashreq.transfercoreservice.fundtransfer.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
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
public class LocalCurrencyValidatorTest { 

	@Mock
    private AsyncUserEventPublisher auditEventPublisher;

    private LocalCurrencyValidator localCurrencyValidator;
    
    @Before
    public void init() {
    	localCurrencyValidator = new LocalCurrencyValidator("EGP", auditEventPublisher);
    	localCurrencyValidator.init();
    }
    
    /**
     * Egypt WAMA transfer.
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Success
     */
    @Test
    public void validate_WAMA_success() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("EGP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("EGP");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(true, result.isSuccess());

    }
    
    /**
     * Egypt WAMA transfer.
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Validation failed because, transaction is not in EGP currency
     */
    @Test
    public void validate_WAMA_txn_USD() { 

        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("USD");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("EGP");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(result.isSuccess(), false);

    }
    
    /**
     * Egypt WYMA transfer.
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Validation failed because, transaction is not in EGP currency
     */
    @Test
    public void validate_WYMA_txn_USD() { 
    	
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setCurrency("USD");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("EGP");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(false, result.isSuccess());

    }
    
    /**
     * Egypt LOCAL transfer.
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Validation failed because, transaction is not in EGP currency
     */
    @Test
    public void validate_LOCAL_txn_USD() { 
    	
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setCurrency("USD");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("EGP");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(false, result.isSuccess());

    }
    
    /**
     * Egypt WAMA transfer.
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Validation failed because destination account is USD account
     */
    @Test
    public void validate_WAMA_destination_USD() { 
    	
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("EGP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("USD");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(false, result.isSuccess());

    }
    
    /**
     * Egypt WYMA transfer.
     * Transaction currency should be EGP
     * Destination account should be EGP account
     * 
     * Validation failed because destination account is USD account
     */
    @Test
    public void validate_WYMA_destination_USD() { 
    	
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("EGP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("EGP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("USD");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(false, result.isSuccess());

    }
    
    /**
     * Egypt LOCAL transfer.
     * If source account is not EGP, transaction currency can be anything other than EGP
     * 
     * Success
     */
    @Test
    public void validate_LOCAL_success() { 
    	
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("USD");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.LOCAL.getName());
        requestDTO.setCurrency("USD");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("USD");
        mockValidationContext.add("credit-account-details", toAccount);

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(true, result.isSuccess());

    }
}
