package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.publisher.Publisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.PaymentHistoryService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FinTxnNoValidatorTest {

    @InjectMocks
    private FinTxnNoValidator finTxnNoValidator;

    @Mock
    private PaymentHistoryService paymentHistoryService;

    @Mock
    private Publisher auditEventPublisher;

    @Test
    public void test_when_fin_tx_id_is_invalid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setFinTxnNo("1234");

        //when
        Mockito.when(paymentHistoryService.isFinancialTransactionPresent(Mockito.eq("1234"))).thenReturn(Boolean.TRUE);
        final ValidationResult result = finTxnNoValidator.validate(requestDTO, null, null);
        //then
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertEquals(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST,result.getTransferErrorCode());

    }

    @Test
    public void test_when_fin_tx_id_is_valid(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setFinTxnNo("1234");

        //when
        Mockito.when(paymentHistoryService.isFinancialTransactionPresent(Mockito.eq("1234"))).thenReturn(Boolean.FALSE);
        final ValidationResult result = finTxnNoValidator.validate(requestDTO, null, null);
        //then
        Assert.assertEquals(true,result.isSuccess());
    }

    @Test(expected = RuntimeException.class)
    public void test_when_fin_tx_id_throws_exception(){
        //given
        FundTransferRequestDTO requestDTO = new FundTransferRequestDTO();
        requestDTO.setFinTxnNo("1234");

        //when
        Mockito.when(paymentHistoryService.isFinancialTransactionPresent(Mockito.eq("1234"))).thenThrow(new RuntimeException());
        final ValidationResult result = finTxnNoValidator.validate(requestDTO, null, null);
        //then

    }



}
