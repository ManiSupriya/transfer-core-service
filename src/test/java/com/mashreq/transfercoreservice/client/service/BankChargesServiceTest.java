package com.mashreq.transfercoreservice.client.service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.client.BankChargesServiceClient;
import com.mashreq.transfercoreservice.client.BeneficiaryClient;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mashreq.transfercoreservice.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankChargesServiceTest {

    @Mock
    private BankChargesServiceClient bankChargesServiceClient;
    @Mock
    private AsyncUserEventPublisher userEventPublisher;

    @InjectMocks
    BankChargesService bankChargesService;

    RequestMetaData metaData = RequestMetaData.builder().build();



    @Test
    public void getTransactionCharges(){
        when(bankChargesServiceClient.getTransactionCharges(any(), any())).thenReturn(getSuccessResponse(getBankCharges()));

        TransactionChargesDto transactionChargesDto = bankChargesService.getTransactionCharges("SAVACR","INR", metaData);

        assertNotNull(transactionChargesDto);
        assertEquals(transactionChargesDto.getAccountClass(), "SAVACR");
    }

    @Test
    public void getTransactionChargesError(){
        when(bankChargesServiceClient.getTransactionCharges(any(), any())).thenReturn(getEmptyErrorResponse());

        Assertions.assertThrows(GenericException.class, () -> bankChargesService.getTransactionCharges("SAVACR","INR", metaData));

    }
}
