package com.mashreq.transfercoreservice.client.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceClient maintenanceClient;

    /**
     * Convert currency from account to transactionCurrency
     */
    public CurrencyConversionDto convertCurrency(CoreCurrencyConversionRequestDto conversionRequestDto) {

        if (StringUtils.equalsIgnoreCase(conversionRequestDto.getAccountCurrency(), conversionRequestDto.getTransactionCurrency())) {
            CurrencyConversionDto sameCurrencyResponse = new CurrencyConversionDto();
            sameCurrencyResponse.setExchangeRate(BigDecimal.ONE);
            sameCurrencyResponse.setAccountCurrencyAmount( conversionRequestDto.getAccountCurrencyAmount() != null ?
                            conversionRequestDto.getAccountCurrencyAmount() : conversionRequestDto.getTransactionAmount());
            sameCurrencyResponse.setTransactionAmount(conversionRequestDto.getAccountCurrencyAmount() != null ?
                    conversionRequestDto.getAccountCurrencyAmount() : conversionRequestDto.getTransactionAmount());
            return sameCurrencyResponse;
        }

        try {
            log.info("[MaintenanceService] calling maintenance service client for currency conversion");
            return maintenanceClient.convertBetweenCurrencies(conversionRequestDto).getData();
        } catch (FeignException e) {
            String error = e.contentUTF8();
            log.error("[MaintenanceService] Error in currency conversion ={} ", error);
            GenericExceptionHandler.handleError(TransferErrorCode.CURRENCY_CONVERSION_FAIL,
                    TransferErrorCode.CURRENCY_CONVERSION_FAIL.getErrorMessage());
            return null;
        }
    }

}