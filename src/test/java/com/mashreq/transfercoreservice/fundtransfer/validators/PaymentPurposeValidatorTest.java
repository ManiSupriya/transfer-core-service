package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.PurposeOfTransferDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PURPOSE_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PURPOSE_DESC;

@RunWith(MockitoJUnitRunner.class)
public class PaymentPurposeValidatorTest {

    @InjectMocks
    private PaymentPurposeValidator paymentPurposeValidator;

    @Test
    public void test_when_pop_code_and_desc_is_valid() {
        //Given
        final Set<PurposeOfTransferDto> purposes = new HashSet<>(Arrays.asList(
                PurposeOfTransferDto.builder().popDesc("Family Support").popCode("FAM").build(),
                PurposeOfTransferDto.builder().popCode("SAL").popDesc("Salary").build()
        ));
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("purposes", purposes);
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setPurposeCode("SAL");
        request.setPurposeDesc("Salary");

        //when
        final ValidationResult result = paymentPurposeValidator.validate(request, null, mockValidationContext);

        //then
        Assert.assertEquals( true, result.isSuccess());

    }

    @Test
    public void test_when_pop_code_is_invalid() {
        //Given
        final Set<PurposeOfTransferDto> purposes = new HashSet<>(Arrays.asList(
                PurposeOfTransferDto.builder().popDesc("Family Support").popCode("FAM").build(),
                PurposeOfTransferDto.builder().popCode("SAL").popDesc("Salary").build()
        ));
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("purposes", purposes);
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setPurposeCode("SAP");
        request.setPurposeDesc("Salary");

        //when
        final ValidationResult result = paymentPurposeValidator.validate(request, null, mockValidationContext);

        //then
        Assert.assertEquals( false, result.isSuccess());
        Assert.assertEquals( INVALID_PURPOSE_CODE, result.getTransferErrorCode());

    }

    @Test
    public void test_when_pop_desc_is_invalid() {
        //Given
        final Set<PurposeOfTransferDto> purposes = new HashSet<>(Arrays.asList(
                PurposeOfTransferDto.builder().popDesc("Family Support").popCode("FAM").build(),
                PurposeOfTransferDto.builder().popCode("SAL").popDesc("Salary").build()
        ));
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("purposes", purposes);
        FundTransferRequestDTO request = new FundTransferRequestDTO();
        request.setPurposeCode("FAM");
        request.setPurposeDesc("Family ");

        //when
        final ValidationResult result = paymentPurposeValidator.validate(request, null, mockValidationContext);

        //then
        Assert.assertEquals( false, result.isSuccess());
        Assert.assertEquals( INVALID_PURPOSE_DESC, result.getTransferErrorCode());

    }

    @Test(expected = NullPointerException.class)
    public void test_when_validation_throws_exception(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setPurposeCode("FAM");
        requestDTO.setPurposeDesc("Family ");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("purposes", null);

        //when
        paymentPurposeValidator.validate(requestDTO, null, mockValidationContext);

    }





}
