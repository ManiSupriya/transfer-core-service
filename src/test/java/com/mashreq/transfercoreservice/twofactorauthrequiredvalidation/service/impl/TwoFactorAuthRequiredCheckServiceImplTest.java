package com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import com.mashreq.transfercoreservice.model.TransferDetails;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.config.TwoFactorAuthRequiredValidationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TwoFactorAuthRequiredCheckServiceImplTest {
    private TwoFactorAuthRequiredCheckServiceImpl service;
    @Mock
    private TwoFactorAuthRequiredValidationConfig config;
    @Mock
    private MaintenanceService maintenanceService;
    @Mock
    private BeneficiaryService beneficiaryService;
    @Mock
    private TransferLimitRepository transferLimitRepository;

    private RequestMetaData metaData = RequestMetaData.builder().primaryCif("primaryCif").build();
    private String localCurrency = "AED";

    @Before
    public void init() {
        service = new TwoFactorAuthRequiredCheckServiceImpl(config, maintenanceService, beneficiaryService,
                transferLimitRepository);
        ReflectionTestUtils.setField(service, "localCurrency", localCurrency);
    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_NotRelaxedInConfig() {
        when(config.getTwofactorAuthRelaxed()).thenReturn(false);
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, new TwoFactorAuthRequiredCheckRequestDto());
        assertNotNull(twoFactorAuthenticationRequired);
        assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_BeneficiaryRecentlyUpdated() {
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(true);
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);
        assertNotNull(twoFactorAuthenticationRequired);
        assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_transactionAmountExceeded() {
        // Given
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        request.setBeneficiaryId("222");
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(config.getMaxAmountAllowed()).thenReturn(1);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
        when(maintenanceService.convertToLocalCurrency(request, metaData, localCurrency)).thenReturn(BigDecimal.TEN);
        when(transferLimitRepository.findTransactionCountAndTotalAmountBetweenDates(any(), any(),
                any())).thenReturn(buildTransferDetails(5L, new BigDecimal(650)));
        // When
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);

        // Then
        assertNotNull(twoFactorAuthenticationRequired);
        assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_transactionCountExceeded() {
        // Given
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        request.setBeneficiaryId("222");
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(config.getMaxAmountAllowed()).thenReturn(50000);
        when(config.getNoOfTransactionsAllowed()).thenReturn(3);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
        when(maintenanceService.convertToLocalCurrency(request, metaData, localCurrency))
                .thenReturn(new BigDecimal(5000));
        when(transferLimitRepository.findTransactionCountAndTotalAmountBetweenDates(any(), any(),
                any())).thenReturn(buildTransferDetails(5L, new BigDecimal(650)));

        // When
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);

        // Then
        assertNotNull(twoFactorAuthenticationRequired);
        assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_ExceptionCase() {
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        request.setBeneficiaryId("222");
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);
        assertNotNull(twoFactorAuthenticationRequired);
        assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_NotRequiredCase() {
        // Given
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        request.setBeneficiaryId("222");
        BigDecimal amount = new BigDecimal(500);
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(config.getMaxAmountAllowed()).thenReturn(10000);
        when(config.getNoOfTransactionsAllowed()).thenReturn(3);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
        when(maintenanceService.convertToLocalCurrency(request, metaData, localCurrency)).thenReturn(amount);
        when(transferLimitRepository.findTransactionCountAndTotalAmountBetweenDates(any(), any(),
                any())).thenReturn(buildTransferDetails(2L, amount));

        // When
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);

        // Then
        assertNotNull(twoFactorAuthenticationRequired);
        assertFalse(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void should_not_require_otp_when_not_transfer_are_done() {
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        request.setBeneficiaryId("222");
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
        when(maintenanceService.convertToLocalCurrency(request, metaData, localCurrency))
                .thenReturn(new BigDecimal(5000));
        when(transferLimitRepository.findTransactionCountAndTotalAmountBetweenDates(any(), any(),
                any())).thenReturn(null);

        // When
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);

        // Then
        assertNotNull(twoFactorAuthenticationRequired);
        assertFalse(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    @Test
    public void should_handle_exception_when_otp_requirement_is_verified() {
        // Given
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);

        // When
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);

        // Then
        assertNotNull(twoFactorAuthenticationRequired);
        assertTrue(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());

    }

    @Test
    public void test_checkIfTwoFactorAuthenticationRequired_NotRequired_when_transfer_limit_has_no_data() {
        // Given
        TwoFactorAuthRequiredCheckRequestDto request = new TwoFactorAuthRequiredCheckRequestDto();
        request.setBeneficiaryId("222");
        BigDecimal amount = new BigDecimal(500);
        when(config.getTwofactorAuthRelaxed()).thenReturn(true);
        when(config.getMaxAmountAllowed()).thenReturn(5000);
        when(beneficiaryService.isRecentlyUpdated(request, metaData, config)).thenReturn(false);
        when(maintenanceService.convertToLocalCurrency(request, metaData, localCurrency)).thenReturn(amount);
        when(transferLimitRepository.findTransactionCountAndTotalAmountBetweenDates(any(), any(),
                any())).thenReturn(buildTransferDetails(-1L, null));

        // When
        TwoFactorAuthRequiredCheckResponseDto twoFactorAuthenticationRequired = service
                .checkIfTwoFactorAuthenticationRequired(metaData, request);

        // Then
        assertNotNull(twoFactorAuthenticationRequired);
        assertFalse(twoFactorAuthenticationRequired.isTwoFactorAuthRequired());
    }

    private TransferDetails buildTransferDetails(Long limit, BigDecimal amount) {
        return new TransferDetails(limit, amount);
    }
}
