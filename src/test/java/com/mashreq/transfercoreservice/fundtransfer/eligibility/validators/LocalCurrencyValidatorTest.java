package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;
import com.mashreq.webcore.dto.response.Response;

/**
 * 
 * @author JasarJa
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalCurrencyValidatorTest {
	
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	
	@Mock
	private MobCommonClient mobCommonClient;
	
	private LocalCurrencyValidator localCurrencyValidator;
	
	private RequestMetaData metadata = RequestMetaData.builder().country("EG").build();
	
	@Before
    public void init() {
		localCurrencyValidator = new LocalCurrencyValidator(auditEventPublisher, mobCommonClient, "EGP");
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
        
        FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
        requestDTO.setServiceType(ServiceType.WAMA.getName());
        requestDTO.setCurrency("EGP");
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

        final ValidationResult result = localCurrencyValidator.validate(requestDTO, null, mockValidationContext);
        Assert.assertEquals(true, result.isSuccess());

    }
}
