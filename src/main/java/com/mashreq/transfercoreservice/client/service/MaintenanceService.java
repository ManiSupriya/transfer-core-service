package com.mashreq.transfercoreservice.client.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.MaintenanceClient;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealEnquiryDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceClient maintenanceClient;

    public static final String DEFAULT_REGION = "AE";

    /**
     * Convert currency from account to transactionCurrency
     */
    public CurrencyConversionDto convertCurrency(CoreCurrencyConversionRequestDto conversionRequestDto) {

        if (StringUtils.equalsIgnoreCase(conversionRequestDto.getAccountCurrency(), conversionRequestDto.getTransactionCurrency())) {
            CurrencyConversionDto sameCurrencyResponse = new CurrencyConversionDto();
            sameCurrencyResponse.setExchangeRate(BigDecimal.ONE);
            sameCurrencyResponse.setAccountCurrencyAmount(conversionRequestDto.getAccountCurrencyAmount() != null ?
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

    public CurrencyConversionDto convertBetweenCurrencies(final CoreCurrencyConversionRequestDto currencyRequest) {
        log.info("[MaintenanceService] calling maintenance service client for converting between currencies");
        return maintenanceClient.convertBetweenCurrencies(currencyRequest).getData();
    }

    public List<CountryMasterDto> getAllCountries(final String channel, final String region, final Boolean active) {
        log.info("[MaintenanceService] calling maintenance service client for getting all countries");
        return maintenanceClient.getAllCountries(channel, region, active).getData();
    }
    
    public DealEnquiryDto getFXDealInformation(final String dealNumber) {
        log.info("[MaintenanceService] calling maintenance service client for getting deal information");
        return maintenanceClient.getFXDealInformation(dealNumber).getBody().getData();
    }

    public List<CoreCurrencyDto> getAllCurrencies() {
        log.info("Fetching all currencies  from maintenance service ");
        Response<List<CoreCurrencyDto>> response = maintenanceClient.getAllCurrencies(DEFAULT_REGION);
        if (ResponseStatus.ERROR == response.getStatus() || Objects.isNull(response.getData())) {
        	Collections.emptyList();
        }
        return response.getData();
    }


}
