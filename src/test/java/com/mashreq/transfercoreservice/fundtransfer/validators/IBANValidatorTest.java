package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class IBANValidatorTest {


    @Mock
    private AsyncUserEventPublisher auditEventPublisher;

    @InjectMocks
    private IBANValidator ibanValidator;

    @Test
    public void test_when_iban_is_invalidEG(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("EG120330001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 29);
        requestDTO.setServiceType("test");
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);
        //then
       assertEquals(false,result.isSuccess());
       assertEquals(TransferErrorCode.IBAN_LENGTH_NOT_VALID,result.getTransferErrorCode());
    }
    @Test
    public void test_when_iban_is_validEG(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("EG120260001015673975601425634");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 29);
        requestDTO.setServiceType("test");
        //when
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);
        //then
       assertEquals(true,result.isSuccess());
    }


    @Test
    public void test_when_iban_is_invalid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE120330001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 23);
        requestDTO.setServiceType("test");

        //when
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);

        //then
       assertEquals(false,result.isSuccess());
       assertEquals(TransferErrorCode.SAME_BANK_IBAN,result.getTransferErrorCode());

    }

    @Test
    public void test_when_iban_length_is_invalid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE1203300010156739756");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 23);
        requestDTO.setServiceType("test");

        //when

          
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);

        //then
       assertEquals(false,result.isSuccess());
       assertEquals(TransferErrorCode.IBAN_LENGTH_NOT_VALID,result.getTransferErrorCode());

    }


    @Test
    public void test_when_iban_is_valid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE120260001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", 23);
        requestDTO.setServiceType("test");

        //when

          
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        final ValidationResult result = ibanValidator.validate(requestDTO, null, mockValidationContext);

        //then
       assertEquals(true,result.isSuccess());

    }

    @Test()
    public void test_when_validation_throws_exception(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setToAccount("AE120260001015673975601");
        ValidationContext mockValidationContext = new ValidationContext();
        mockValidationContext.add("iban-length", null);
        requestDTO.setServiceType("test");


        //when
          
        ReflectionTestUtils.setField(ibanValidator,"bankCode", "033");
        //then
        assertThrows(NullPointerException.class, () -> ibanValidator.validate(requestDTO, null, mockValidationContext));
    }


}
