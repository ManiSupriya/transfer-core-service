package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CharityValidatorTest {

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private CharityValidator charityValidator;

    @Test
    public void test_when_validation_passes() {

        //given
        CharityBeneficiaryDto charityBeneficiaryDto = new CharityBeneficiaryDto();
        charityBeneficiaryDto.setAccountNumber("019010073766");
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("test");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("charity-beneficiary-dto", charityBeneficiaryDto);

        //when
        final ValidationResult result = charityValidator.validate(requestDTO, null, mockValidationContext);

        //then
       assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_when_validation_fails() {

        //given
        CharityBeneficiaryDto charityBeneficiaryDto = new CharityBeneficiaryDto();
        charityBeneficiaryDto.setAccountNumber("019010073744");
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        requestDTO.setServiceType("test");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("charity-beneficiary-dto", charityBeneficiaryDto);

        //when
        final ValidationResult result = charityValidator.validate(requestDTO, null, mockValidationContext);

        //then
       assertEquals(result.isSuccess(), false);

    }

    @Test()
    public void test_when_validation_fails_throws_exception() {

        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        ValidationContext mockValidationContext = new ValidationContext();
        requestDTO.setServiceType("test");
        mockValidationContext.add("charity-beneficiary-dto", null);
        assertThrows(NullPointerException.class, () ->charityValidator.validate(requestDTO, null, mockValidationContext));
    }

}
