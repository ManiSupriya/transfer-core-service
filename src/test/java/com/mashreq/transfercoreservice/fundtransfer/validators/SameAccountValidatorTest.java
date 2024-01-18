package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.HtmlUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SameAccountValidatorTest {

    @InjectMocks
    private SameAccountValidator sameAccountValidator;

    @Mock
    private AsyncUserEventPublisher auditEventPublisher;
    @Mock
    HtmlUtils htmlUtils;

    @Test
    public void test_from_acct_to_acct_is_same() {
        //Given
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setFromAccount("019010073766");
        fundTransferRequestDTO.setToAccount("019010073766");
        fundTransferRequestDTO.setServiceType("test");
        fundTransferRequestDTO.setFinTxnNo("1234");

        //When
        final ValidationResult result = sameAccountValidator.validate(fundTransferRequestDTO, null, null);

        //Then
        assertEquals(result.isSuccess(), false);
        assertEquals(result.getTransferErrorCode(), TransferErrorCode.CREDIT_AND_DEBIT_ACC_SAME);

    }

    @Test
    public void test_from_acct_to_acct_is_different() {
        //Given
        FundTransferRequestDTO fundTransferRequestDTO = new FundTransferRequestDTO();
        fundTransferRequestDTO.setFromAccount("019010073766");
        fundTransferRequestDTO.setToAccount("019014473766");
        fundTransferRequestDTO.setServiceType("test");
        fundTransferRequestDTO.setFinTxnNo("1234");
        //When
        final ValidationResult result = sameAccountValidator.validate(fundTransferRequestDTO, null, null);
        //Then
        assertEquals(result.isSuccess(), true);

    }



}
