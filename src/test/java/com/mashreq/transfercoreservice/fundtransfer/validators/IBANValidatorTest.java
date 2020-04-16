package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class IBANValidatorTest {

    @InjectMocks
    private IBANValidator ibanValidator;


    @Test
    public void test_when_iban_is_invalid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE120330001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 23);
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        //when
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertEquals(TransferErrorCode.SAME_BANK_IBAN,result.getTransferErrorCode());

    }

    @Test
    public void test_when_iban_length_is_invalid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE1203300010156739756");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 23);
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        //when
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertEquals(TransferErrorCode.IBAN_LENGTH_NOT_VALID,result.getTransferErrorCode());

    }


    @Test
    public void test_when_iban_is_valid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE120260001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 23);
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        //when
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);

        //then
        Assert.assertEquals(true,result.isSuccess());

    }

    @Test(expected = NullPointerException.class)
    public void test_when_validation_throws_exception(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE120260001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", null);
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");

        //when
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);
        //then

    }


}
