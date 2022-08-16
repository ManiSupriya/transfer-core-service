package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
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
public class EgyptLocalCurrencyEligibilityValidatorTest {
	
	@Mock
	private LocalCurrencyValidations localCurrencyValidations;
	
	@InjectMocks
	private EgyptLocalCurrencyEligibilityValidator localCurrencyValidator;

	
	private RequestMetaData metadata = RequestMetaData.builder().country("EG").build();
	
	@Test
	public void testValidate() {
		
		AccountDetailsDTO accountDetailsDTO = new AccountDetailsDTO();
		accountDetailsDTO.setNumber("019010073766");
		accountDetailsDTO.setCurrency("EGP");
		ValidationContext mockValidationContext = new ValidationContext();
		
		FundTransferEligibiltyRequestDTO requestDTO = new FundTransferEligibiltyRequestDTO();
		requestDTO.setServiceType(ServiceType.WYMA.getName());
		requestDTO.setTxnCurrency("EGP");
		
		AccountDetailsDTO toAccountDetailsDTO = new AccountDetailsDTO();
		toAccountDetailsDTO.setNumber("019010073766");
		toAccountDetailsDTO.setCurrency("EGP");
		
		mockValidationContext.add("from-account", accountDetailsDTO);
		mockValidationContext.add("to-account", toAccountDetailsDTO);
		
		Mockito.when(localCurrencyValidations.performLocalCurrencyChecks(Mockito.any())).thenReturn(ValidationResult.builder().success(true).build());
		ValidationResult  result = localCurrencyValidator.validate(requestDTO, metadata, mockValidationContext);
		assertEquals(true, result.isSuccess());
	}
}
