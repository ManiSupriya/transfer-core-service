package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CharityValidatorTest {

    @InjectMocks
    private CharityValidator charityValidator;

    @Test
    public void test_when_validation_passes() {

        //given
        CharityBeneficiaryDto charityBeneficiaryDto = new CharityBeneficiaryDto();
        charityBeneficiaryDto.setAccountNumber("019010073766");
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("charity-beneficiary-dto", charityBeneficiaryDto);

        //when
        final ValidationResult result = charityValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), true);

    }

    @Test
    public void test_when_validation_fails() {

        //given
        CharityBeneficiaryDto charityBeneficiaryDto = new CharityBeneficiaryDto();
        charityBeneficiaryDto.setAccountNumber("019010073744");
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("019010073766");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("charity-beneficiary-dto", charityBeneficiaryDto);

        //when
        final ValidationResult result = charityValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(result.isSuccess(), false);

    }

    @Test(expected = NullPointerException.class)
    public void test_when_validation_fails_throws_exception() {

        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("charity-beneficiary-dto", null);

        //when
        final ValidationResult result = charityValidator.validate(requestDTO, null, mockValidationContext);

    }

}
