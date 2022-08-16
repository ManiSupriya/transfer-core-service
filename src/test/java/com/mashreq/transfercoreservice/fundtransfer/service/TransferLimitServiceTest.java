package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import com.mashreq.transfercoreservice.model.TransferLimit;
import com.mashreq.transfercoreservice.transactionqueue.TransactionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.mashreq.transfercoreservice.util.TestUtil.buildTransferLimitRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransferLimitServiceTest {

    @Mock
    TransferLimitRepository repository;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransferLimitService service;

    @Test
    public void shouldSaveTransferDetail() {
        // Given

        // When
        service.saveTransferDetails(buildTransferLimitRequest(), "WQNI16082285457");

        // Then
        verify(repository, times(1)).save(any());
    }

    @Test
    public void should_validate_and_save_transfer_details() {
        // Given

        // When
        TransferLimitResponseDto responseDto = service.validateAndSaveTransferDetails(buildTransferLimitRequest(),
                "WQNI11082285105");

        // Then
        verify(repository, times(1)).save(any());
        assertNotNull(responseDto);
        assertTrue(responseDto.isSuccess());
    }

    @Test
    public void should_not_save_when_transaction_is_already_present() {
        // Given
        when(repository.findByTransactionRefNo(any())).thenReturn(Optional.of(new TransferLimit()));

        // When
        TransferLimitResponseDto responseDto = service.validateAndSaveTransferDetails(buildTransferLimitRequest(),
                "WQNI11082285105");

        // Then
        verify(repository, never()).save(any());
        assertNotNull(responseDto);
        assertFalse(responseDto.isSuccess());
        assertEquals("TC-409", responseDto.getErrorCode());
        assertEquals("Duplicate entry found for the transaction reference no", responseDto.getErrorMessage());
    }

    @Test
    public void should_handle_exception_while_saving_transfer_details() {
        // Given
        when(repository.findByTransactionRefNo(any())).thenThrow(new RuntimeException(
                "Error"));

        // When
        TransferLimitResponseDto responseDto = service.validateAndSaveTransferDetails(buildTransferLimitRequest(),
                "WQNI11082285105");

        // Then
        verify(repository, never()).save(any());
        assertNotNull(responseDto);
        assertFalse(responseDto.isSuccess());
        assertEquals("TC-500", responseDto.getErrorCode());
        assertEquals("Error occurred while saving transfer details", responseDto.getErrorMessage());
    }
}