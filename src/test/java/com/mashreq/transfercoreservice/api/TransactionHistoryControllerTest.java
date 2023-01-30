package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class TransactionHistoryControllerTest {
    @Mock
    private TransactionHistoryService transactionHistoryService;

    private TransactionHistoryController controller;

    @Before
    public void init() {
        controller = new TransactionHistoryController(transactionHistoryService);
    }

    @Test()
    public void test_saveTransactionHistory() {
        RequestMetaData metaData = getMetaData();
        TransactionHistoryDto transactionHistoryDto;
        when(transactionHistoryService.saveTransactionHistory(any(), any())).thenReturn(2L);
        controller.saveTransactionHistory(metaData, new TransactionHistoryDto());
    }

    @Test()
    public void getTransactionHistoryTest() {
        when(transactionHistoryService.getTransactionHistoryByCif(any(),any(),any())).thenReturn(Arrays.asList(TransactionHistoryDto.builder()
                .hostReferenceNo("HOST12345")
                .build()));
        Response<List<TransactionHistoryDto>> response = controller.getTransactionHistory("Payment12356","2021-04-24","2021-04-29");
        assertNotNull(response.getData());
        assertEquals("HOST12345", response.getData().get(0).getHostReferenceNo());
    }

    private RequestMetaData getMetaData() {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif("22231312");
        return metaData;
    }

}
