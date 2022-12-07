package com.mashreq.transfercoreservice.mapper;

import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TransactionHistoryMapperTest {

    @Test
    public void transactionHistoryMapperTest() {
        TransactionHistory transactionHistory = TransactionHistory.builder().accountTo("HASS123456")
                .accountFrom("BHU1234567").cif("CIF123345")
                .hostReferenceNo("HOSTREF2134").build();
        TransactionHistoryDto transactionHistoryDto = TransactionHistoryMapper.getTransactionHistoryDto(transactionHistory);
        assertEquals("HOSTREF2134", transactionHistoryDto.getHostReferenceNo());
        assertEquals("BHU1234567", transactionHistoryDto.getAccountFrom());
        assertEquals("HASS123456", transactionHistoryDto.getAccountTo());
    }
}
