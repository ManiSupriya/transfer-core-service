package com.mashreq.transfercoreservice.fundtransfer.validators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
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
	private LocalCurrencyValidations localCurrencyValidations;

	@InjectMocks
    private EgyptLocalCurrencyValidator egyptLocalCurrencyValidator;

    private RequestMetaData metadata = RequestMetaData.builder().country("EG").build();
    
    @Test
	public void testValidate() {

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

        
        Mockito.when(localCurrencyValidations.performLocalCurrencyChecks(Mockito.any())).thenReturn(ValidationResult.builder().success(true).build());
        final ValidationResult result = egyptLocalCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
        assertEquals(true, result.isSuccess());

    }
}
