package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.transfercoreservice.client.service.BankChargesService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/***
 * @author shilpin
 */
@ExtendWith(MockitoExtension.class)
public class UAEBankChargesServiceTest {
    @InjectMocks
    private UAEBankChargesService uaeBankChargesService;
    @Mock
    private BankChargesService bankChargesService;

    @Test
    public void throw_error_when_charge_bearer_not_provided(){
        //given
        //when
        //then
        GenericException genericException = assertThrows(GenericException.class, () -> uaeBankChargesService
                .getBankFeesForCustomerByCharge(FundTransferRequest.builder().serviceType("INFT")
                        .amount(new BigDecimal("100"))
                        .beneficiaryBankCountry("United Kingdom")
                        .txnCurrency("GBP")
                        .build(), RequestMetaData.builder().build(), ServiceType.INFT));
        assertEquals("TN-1034", genericException.getErrorCode());
        assertEquals("Invalid Charge Bearer", genericException.getMessage());
    }

    @Test
    public void success_when_get_bank_fees(){
        //given
        TransactionChargesDto bankCharges = new TransactionChargesDto();
        bankCharges.setInternationalTransactionalCharge(Double.valueOf("25"));
        //when
        when(bankChargesService.getTransactionCharges(anyString(), anyString(), any())).thenReturn(bankCharges);
        //then
        String charges = uaeBankChargesService.getBankFeesForCustomerByCharge(FundTransferRequest.builder()
                .chargeBearer("U")
                .accountClass("CONV")
                .txnCurrency("GBP")
                .build(), RequestMetaData.builder().build(), ServiceType.INFT);
        assertNotNull(charges);
    }

    @Test
    public void success_when_get_empty_bank_fees_for_charge_bearer_B(){
        //given
        TransactionChargesDto bankCharges = new TransactionChargesDto();
        bankCharges.setInternationalTransactionalCharge(Double.valueOf("25"));
        //when
        when(bankChargesService.getTransactionCharges(anyString(), anyString(), any())).thenReturn(bankCharges);
        //then
        String charges = uaeBankChargesService.getBankFeesForCustomerByCharge(FundTransferRequest.builder()
                .chargeBearer("B")
                .accountClass("CONV")
                .txnCurrency("GBP")
                .build(), RequestMetaData.builder().build(), ServiceType.INFT);
        assertTrue(charges.isEmpty());
    }

    @Test
    public void throw_error_when_charge_bearer_provided_but_invalid(){
        //given
        TransactionChargesDto bankCharges = new TransactionChargesDto();
        bankCharges.setInternationalTransactionalCharge(Double.valueOf("25"));
        //when
        when(bankChargesService.getTransactionCharges(anyString(), anyString(), any())).thenReturn(bankCharges);
        //then
        GenericException genericException = assertThrows(GenericException.class, () -> uaeBankChargesService
                .getBankFeesForCustomerByCharge(FundTransferRequest.builder().serviceType("INFT")
                        .chargeBearer("A")
                        .accountClass("CONV")
                        .amount(new BigDecimal("100"))
                        .beneficiaryBankCountry("United Kingdom")
                        .txnCurrency("GBP")
                        .build(), RequestMetaData.builder().build(), ServiceType.INFT));
        assertEquals("TN-1034", genericException.getErrorCode());
        assertEquals("Invalid Charge Bearer", genericException.getMessage());
    }

    @Test
    public void success_when_get_bank_fees_for_local_transfer(){
        //given
        TransactionChargesDto bankCharges = new TransactionChargesDto();
        bankCharges.setInternationalTransactionalCharge(Double.valueOf("25"));
        //when
        when(bankChargesService.getTransactionCharges(anyString(), anyString(), any())).thenReturn(bankCharges);
        //then
        String charges = uaeBankChargesService.getBankFeesForCustomerByCharge(FundTransferRequest.builder()
                .chargeBearer("O")
                .accountClass("CONV")
                .txnCurrency("GBP")
                .build(), RequestMetaData.builder().build(), ServiceType.LOCAL);
        assertNotNull(charges);
    }
}
