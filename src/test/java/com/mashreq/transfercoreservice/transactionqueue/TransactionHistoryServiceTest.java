package com.mashreq.transfercoreservice.transactionqueue;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.dto.CharityPaidDto;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.mashreq.transfercoreservice.common.CommonConstants.CARD_LESS_CASH;
import static com.mashreq.transfercoreservice.common.CommonConstants.MOB_CHANNEL;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.DB_CONNECTIVITY_ISSUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionHistoryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionHistoryService transactionHistoryService;

    @Test
    public void testSaveTransactionHistory() {
        RequestMetaData metaData = getMetaData("012960010");
        NpssEnrolmentRepoDTO dbResult = new NpssEnrolmentRepoDTO();
        dbResult.setCif_id("012960010");
        dbResult.setEnrollment_status("ENROLLED");
        Optional<NpssEnrolmentRepoDTO> npssUser = Optional.of(dbResult);
        when(transactionRepository.save(any())).thenReturn(getTransactionHistory());
        transactionHistoryService.saveTransactionHistory(getTransactionHistoryDto(), getMetaData("162362"));
    }

    @Test
    public void testIsFinancialTransactionPresent() {
        RequestMetaData metaData = getMetaData("012960010");
        when(transactionRepository.existsPaymentHistoryByFinancialTransactionNo(anyString())).thenReturn(true);
        transactionHistoryService.isFinancialTransactionPresent("162362");
    }

    @Test
    public void testGetCharityPaid() {
        RequestMetaData metaData = getMetaData("012960010");
        when(transactionRepository.findSumByCifIdAndServiceType(anyString(), anyString(), anyString())).thenReturn(new ArrayList<Object[]>());
        transactionHistoryService.getCharityPaid("162362", "TYPE");
    }

    @Test
    public void testGetCharityPaidWithData() {
        List<Object[]> tmpObj = new ArrayList<Object[]>();
        tmpObj.add(new Object[]{new BigDecimal(23)});
        when(transactionRepository
                .findSumByCifIdAndServiceType(any(), any(), any())).thenReturn(tmpObj);
        CharityPaidDto charityPaidDto = transactionHistoryService.getCharityPaid("162362", "TYPE");
       assertEquals(new BigDecimal(23),charityPaidDto.getTotalPaidAmount());
    }

    @Test
    public void getTransactionHistoryPositiveScenarioTest() {
        when(transactionRepository.findAllByCifAndCreatedDate(anyString(),any(),any()))
                .thenReturn(Arrays.asList(TransactionHistory.builder().hostReferenceNo("HOST12345").build()));
        List<TransactionHistoryDto> transactionHistoryDto = transactionHistoryService.getTransactionHistoryByCif("162362","2021-04-22","2021-04-29");
        assertEquals("HOST12345", transactionHistoryDto.get(0).getHostReferenceNo());
    }

    @Test
    public void getTransactionHistoryPositiveScenarioDateWithInvalidFormatTest() {
        when(transactionRepository.findAllByCifAndCreatedDate(anyString(),any(),any()))
                .thenReturn(Arrays.asList(TransactionHistory.builder().hostReferenceNo("HOST12345").build()));
        List<TransactionHistoryDto> transactionHistoryDto = transactionHistoryService
                .getTransactionHistoryByCif("162362","2021-04-22","2021-04-29");
        assertEquals("HOST12345", transactionHistoryDto.get(0).getHostReferenceNo());
    }

    @Test
    public void getTransactionHistoryByRefNumPositiveScenarioTest() {
        when(transactionRepository.findByHostReferenceNo(anyString()))
                .thenReturn(TransactionHistory.builder().hostReferenceNo("HOST12345").build());
        TransactionHistoryDto transactionHistoryDto = transactionHistoryService.getTransactionDetailByHostRef("162362");
        assertEquals("HOST12345", transactionHistoryDto.getHostReferenceNo());
    }

    @Test
    public void getTransactionHistoryByRefNumNegativeScenarioTest() {
        when(transactionRepository.findByHostReferenceNo(anyString()))
                .thenThrow(new IllegalArgumentException());
        GenericException genericException = assertThrows(GenericException.class,
                () -> transactionHistoryService.getTransactionDetailByHostRef("162362"));
        assertEquals(DB_CONNECTIVITY_ISSUE.getErrorMessage(), genericException.getMessage());
        assertEquals(DB_CONNECTIVITY_ISSUE.getCustomErrorCode(), genericException.getErrorCode());
    }

    @Test
    public void getTransactionHistoryNegativeScenarioTest() {
        when(transactionRepository.findAllByCifAndCreatedDate(anyString(),any(),any()))
                .thenThrow(new IllegalArgumentException());
        GenericException genericException = assertThrows(GenericException.class,
                () -> transactionHistoryService.getTransactionHistoryByCif("162362","2021-04-22","2021-04-29"));
        assertEquals(DB_CONNECTIVITY_ISSUE.getErrorMessage(), genericException.getMessage());
        assertEquals(DB_CONNECTIVITY_ISSUE.getCustomErrorCode(), genericException.getErrorCode());
    }

    private RequestMetaData getMetaData(String cif) {
        RequestMetaData metaData = new RequestMetaData();
        metaData.setPrimaryCif(cif);
        return metaData;
    }

    private TransactionHistory getTransactionHistory() {
        TransactionHistory transactionHistory = TransactionHistory.builder().cif("").
                userId(null).channel(MOB_CHANNEL).transactionTypeCode(CARD_LESS_CASH).
                paidAmount(new BigDecimal(8)).status("ERT").
                ipAddress("").mwResponseDescription("").
                accountFrom("").accountTo("").
                billRefNo("").valueDate(LocalDateTime.now()).createdDate(Instant.now()).
                transactionRefNo("").financialTransactionNo("").build();
        return transactionHistory;
    }

    private TransactionHistoryDto getTransactionHistoryDto() {
        TransactionHistoryDto transactionHistory = TransactionHistoryDto.builder().cif("").
                userId(null).channel(MOB_CHANNEL).transactionTypeCode(CARD_LESS_CASH).
                paidAmount(new BigDecimal(8)).status("ERT").
                ipAddress("").mwResponseDescription("").
                accountFrom("").accountTo("").
                billRefNo("").valueDate(LocalDateTime.now()).createdDate(Instant.now()).
                transactionRefNo("").financialTransactionNo("").build();
        return transactionHistory;
    }
}
