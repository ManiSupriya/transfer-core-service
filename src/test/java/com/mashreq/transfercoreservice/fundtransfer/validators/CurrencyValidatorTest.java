package com.mashreq.transfercoreservice.fundtransfer.validators;


import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CurrencyValidatorTest {

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private CurrencyValidator currencyValidator;
    
    @Before
    public void init() {
    	currencyValidator.init();
    }

    @Test
    public void test_within_mashreq_requested_currecy_is_source_currency() {

        //given
        AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
        		accountDetailsDTO.setNumber("019010073766");
        		accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("AED");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("USD");
        mockValidationContext.add("credit-account-details", toAccount);
        //when
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_charity_bait_al_khair_requested_currecy_is_source_currency() {

        //given
        AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
        accountDetailsDTO.setNumber("019010073766");
        accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.BAIT_AL_KHAIR.getName());
        requestDTO.setCurrency("AED");

        CharityBeneficiaryDto beneficiaryDto = new CharityBeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setCurrencyCode("AED");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("charity-beneficiary-dto", beneficiaryDto);

        //when
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_charity_dubai_khair_requested_currecy_is_source_currency() {

        //given
    	AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
        accountDetailsDTO.setNumber("019010073766");
        accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.DUBAI_CARE.getName());
        requestDTO.setCurrency("AED");

        CharityBeneficiaryDto beneficiaryDto = new CharityBeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setCurrencyCode("AED");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("charity-beneficiary-dto", beneficiaryDto);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_charity_dar_al_bair_requested_currecy_is_source_currency() {

        //given
    	AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
        accountDetailsDTO.setNumber("019010073766");
        accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.DAR_AL_BER.getName());
        requestDTO.setCurrency("AED");

        CharityBeneficiaryDto beneficiaryDto = new CharityBeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setCurrencyCode("AED");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("charity-beneficiary-dto", beneficiaryDto);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_charity_dar_al_bair_requested_currecy_is_destination_currency() {

        //given
    	AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
        accountDetailsDTO.setNumber("019010073766");
        accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.DAR_AL_BER.getName());
        requestDTO.setCurrency("AED");

        CharityBeneficiaryDto beneficiaryDto = new CharityBeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setCurrencyCode("AED");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("charity-beneficiary-dto", beneficiaryDto);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals( true, result.isSuccess());

    }

    @Test
    public void test_charity_dar_al_bair_requested_currecy_is_invalid_currency() {

        //given
        AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
        accountDetailsDTO.setNumber("019010073766");
        accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.DAR_AL_BER.getName());
        requestDTO.setCurrency("GBP");

        CharityBeneficiaryDto beneficiaryDto = new CharityBeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setCurrencyCode("AED");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("charity-beneficiary-dto", beneficiaryDto);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getTransferErrorCode(), TransferErrorCode.CURRENCY_IS_INVALID);

    }


    @Test
    public void test_own_cif_requested_currecy_is_source_currency() {

        //given
    	AccountDetailsDTO toAccount =  new AccountDetailsDTO();
    	toAccount.setNumber("019010073766");
    	toAccount.setCurrency("AED");
        AccountDetailsDTO fromAccount =  new AccountDetailsDTO();
        fromAccount.setNumber("019010073789");
        fromAccount.setCurrency("GBP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setCurrency("AED");



        mockValidationContext.add("from-account", fromAccount);
        mockValidationContext.add("to-account", toAccount);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_within_mashreq_requested_currecy_is_destination_currency() {

        //given
    	 AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
         accountDetailsDTO.setNumber("019010073766");
         accountDetailsDTO.setCurrency("AED");
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
        toAccount.setCurrency("USD");
        mockValidationContext.add("credit-account-details", toAccount);
        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_own_cif_requested_currecy_is_destination_currency() {

        //given
    	AccountDetailsDTO toAccount =  new AccountDetailsDTO();
    	toAccount.setNumber("019010073766");
    	toAccount.setCurrency("AED");
        AccountDetailsDTO fromAccount =  new AccountDetailsDTO();
        fromAccount.setNumber("019010073789");
        fromAccount.setCurrency("GBP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setCurrency("GBP");



        mockValidationContext.add("from-account", fromAccount);
        mockValidationContext.add("to-account", toAccount);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_within_mashreq_requested_currecy_is_invalid_currency() {

        //given
    	AccountDetailsDTO accountDetailsDTO =  new AccountDetailsDTO();
    	accountDetailsDTO.setNumber("019010073766");
    	accountDetailsDTO.setCurrency("AED");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("GBP");

        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        mockValidationContext.add("from-account", accountDetailsDTO);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);
        SearchAccountDto toAccount = new SearchAccountDto();
        toAccount.setCurrency("USD");
        mockValidationContext.add("credit-account-details", toAccount);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getTransferErrorCode(), TransferErrorCode.CURRENCY_IS_INVALID);

    }

    @Test
    public void test_own_cif_requested_currecy_is_invalid_currency() {

        //given
    	AccountDetailsDTO toAccount =  new AccountDetailsDTO();
    	toAccount.setNumber("019010073766");
    	toAccount.setCurrency("AED");
        AccountDetailsDTO fromAccount =  new AccountDetailsDTO();
        fromAccount.setNumber("019010073789");
        fromAccount.setCurrency("GBP");
        ValidationContext mockValidationContext = new ValidationContext();
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WYMA.getName());
        requestDTO.setCurrency("USD");



        mockValidationContext.add("from-account", fromAccount);
        mockValidationContext.add("to-account", toAccount);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), false);
        Assert.assertEquals(result.getTransferErrorCode(), TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH);

    }

    @Test(expected = NullPointerException.class)
    public void test_when_validation_fails_throws_exception() {

        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("USD");


        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setAccountNumber("019022073766");
        beneficiaryDto.setStatus(BeneficiaryStatus.ACTIVE.getValue());

        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("from-account", null);
        mockValidationContext.add("beneficiary-dto", beneficiaryDto);

        //when
         
        final ValidationResult result = currencyValidator.validate(requestDTO, null, mockValidationContext);

    }

}
