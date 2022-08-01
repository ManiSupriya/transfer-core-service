package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.TransferLimitDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static com.mashreq.transfercoreservice.paylater.enums.FTOrderType.PL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class TransferLimitServiceTest {

    @Mock
    TransferLimitRepository repository;

    @InjectMocks
    TransferLimitService service;

    @Test
    public void shouldSaveTransferDetail() {
        // Given

        // When
        service.saveTransferDetails(TransferLimitDto.builder()
                .amount(new BigDecimal(100))
                .beneficiaryId(123L)
                .orderType(PL)
                .build());

        // Then
        Mockito.verify(repository, times(1)).save(any());
    }
}