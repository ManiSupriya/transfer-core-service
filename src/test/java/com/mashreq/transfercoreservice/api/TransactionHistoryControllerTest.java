package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.CharityPaidDto;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class TransactionHistoryControllerTest {
    @Mock
    private TransactionHistoryService transactionHistoryService;

    private TransactionHistoryController controller;

    @BeforeEach
    public void init() {
        controller = new TransactionHistoryController(transactionHistoryService);
    }

    @Test
    public void test_saveTransactionHistory() {
        RequestMetaData metaData = getMetaData();
        TransactionHistoryDto transactionHistoryDto;
        when(transactionHistoryService.saveTransactionHistory(any(), any())).thenReturn(2L);
        controller.saveTransactionHistory(metaData, new TransactionHistoryDto());
    }

    @Test
    public void getTransactionHistoryTest() {
        when(transactionHistoryService.getTransactionHistoryByCif(any(), any(), any())).thenReturn(Arrays.asList(TransactionHistoryDto.builder()
                .hostReferenceNo("HOST12345")
                .build()));
        Response<List<TransactionHistoryDto>> response = controller.getTransactionHistory("Payment12356", "2021-04-24", "2021-04-29");
        assertNotNull(response.getData());
        assertEquals("HOST12345", response.getData().get(0).getHostReferenceNo());
    }

    @Test
    public void transferFundsTest() {
        CharityPaidDto res = CharityPaidDto.builder().totalPaidAmount(new BigDecimal("233232")).build();
        when(transactionHistoryService.getCharityPaid(any(), any())).thenReturn(res);
        Response<CharityPaidDto> response = controller.transferFunds(RequestMetaData.builder().build(), "", "");
        assertNotNull(response.getData());
        assertEquals(new BigDecimal("233232"), response.getData().getTotalPaidAmount());
    }

    @Test
    public void getTransactionDetailTest() {
        TransactionHistoryDto res = TransactionHistoryDto.builder().hostReferenceNo("123453213421").build();
        when(transactionHistoryService.getTransactionDetailByHostRef(any())).thenReturn(res);
        Response<TransactionHistoryDto> response = controller.getTransactionDetail("1234532");
        assertNotNull(response.getData());
        assertEquals("123453213421", response.getData().getHostReferenceNo());
    }

    private RequestMetaData getMetaData() {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif("22231312");
        return metaData;
    }

}
