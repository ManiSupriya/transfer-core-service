package com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.impl;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.repository.TransferLimitRepository;
import com.mashreq.transfercoreservice.model.TransferDetails;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.config.TwoFactorAuthRequiredValidationConfig;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthRequiredCheckServiceImpl implements TwoFactorAuthRequiredCheckService {
    private final TwoFactorAuthRequiredValidationConfig config;
    private final MaintenanceService maintenanceService;
    private final BeneficiaryService beneficiaryService;

    private final TransferLimitRepository transferLimitRepository;
    @Value("${app.local.currency}")
    private String localCurrency;

    @Override
    public TwoFactorAuthRequiredCheckResponseDto checkIfTwoFactorAuthenticationRequired(RequestMetaData metaData,
                                                                                        TwoFactorAuthRequiredCheckRequestDto requestDto) {
        TwoFactorAuthRequiredCheckResponseDto dto = new TwoFactorAuthRequiredCheckResponseDto();
        dto.setTwoFactorAuthRequired(true);
        try {
            if (!config.getTwofactorAuthRelaxed() || beneficiaryService.isRecentlyUpdated(requestDto, metaData,
                    config)) {
                return dto;
            }
            /*account number belongs to cif validation needs to be done.*/
            TransferDetails transferDetails = transferLimitRepository.findTransactionCountAndTotalAmountBetweenDates(
                    Long.valueOf(requestDto.getBeneficiaryId()),
                    now().minus(config.getDurationInHours(), HOURS), now());
            BigDecimal localCurrencyAmount = maintenanceService.convertToLocalCurrency(requestDto, metaData, localCurrency);
            dto.setTwoFactorAuthRequired(isLimitExceeded(localCurrencyAmount, transferDetails));
        } catch (Exception e) {
            log.error("Error occurred while evaluating OTP requirement", e);
        }
        return dto;
    }

    private boolean isLimitExceeded(BigDecimal localCurrencyAmount, TransferDetails transferLimit) {
        if (isNull(transferLimit)) {
            return false;
        }
        return isTransactionAmountExceeded(localCurrencyAmount, transferLimit)
                || isTransactionCountExceeded(transferLimit);
    }

    private boolean isTransactionAmountExceeded(BigDecimal localCurrencyAmount, TransferDetails transferLimit) {
        BigDecimal totalAmount = transferLimit.getAmount();
        if (isNull(totalAmount)) {
            totalAmount = BigDecimal.ZERO;
        }
        totalAmount = totalAmount.add(localCurrencyAmount);
        return totalAmount.compareTo(new BigDecimal(config.getMaxAmountAllowed())) > 0;
    }

    private boolean isTransactionCountExceeded(TransferDetails transferLimit) {
        long transactionCount = transferLimit.getTransfers();
        return transactionCount >= config.getNoOfTransactionsAllowed();
    }
}
