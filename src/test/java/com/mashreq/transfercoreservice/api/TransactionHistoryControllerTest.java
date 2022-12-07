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
        RequestMetaData metaData = getMetaData();
        TransactionHistoryDto transactionHistoryDto = TransactionHistoryDto.builder().hostReferenceNo("HOST12345").build();
        when(transactionHistoryService.getTransactionHistory(any(), any())).thenReturn(transactionHistoryDto);
        Response response = controller.getTransactionHistory(metaData, "Payment12356");
		assertNotNull(response);
    }

    private RequestMetaData getMetaData() {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif("22231312");
        return metaData;
    }

}
