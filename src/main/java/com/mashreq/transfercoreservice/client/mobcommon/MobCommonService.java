package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.ErrorUtils;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.webcore.dto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_NOT_FOUND;
import static java.time.Instant.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobCommonService {

    private final MobCommonClient mobCommonClient;
    public static final String TRUE = "true";

    public LimitValidatorResultsDto validateAvailableLimit(String cifId, String beneficiaryTypeCode, BigDecimal amount) {
        Instant startTime = Instant.now();
        log.info("[MobCommonService] Calling MobCommonService for limit validation for cif={} beneficiaryTypeCode = {} " +
                        "and amount ={}",
                cifId, beneficiaryTypeCode, amount);

        Response<LimitValidatorResultsDto> limitValidatorResultsDtoResponse =
                mobCommonClient.validateAvailableLimit(cifId, beneficiaryTypeCode, amount);

        if (TRUE.equalsIgnoreCase(limitValidatorResultsDtoResponse.getHasError())) {
            final String errorDetails = getErrorDetails(limitValidatorResultsDtoResponse);
            log.error("[MobCommonService] Exception in calling mob customer for limit validation ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);
        }
        log.info("[MobCommonService] Limit validation response success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return limitValidatorResultsDtoResponse.getData();
    }

    public Set<MoneyTransferPurposeDto> getPaymentPurposes(String channelTraceId, String transactionType, String countryIsoCode) {
        log.info("[MobCommonService] Calling MobCommonService for getting POP for QR transfer to country={}  ",
                countryIsoCode);
        Instant startTime = now();
        final Response<Set<MoneyTransferPurposeDto>> paymentPurpose = mobCommonClient.getPaymentPurpose(channelTraceId, transactionType, countryIsoCode);

        if (TRUE.equalsIgnoreCase(paymentPurpose.getHasError())) {
            final String errorDetails = getErrorDetails(paymentPurpose);
            log.error("[MobCommonService] Exception in calling mob customer for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);
        }
        log.info("[MobCommonService] Payment purpose response success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return paymentPurpose.getData();
    }

    public CurrencyConversionDto getConvertBetweenCurrencies(CoreCurrencyConversionRequestDto currencyRequest) {
        log.info("[MobCommonService] Calling currency conversion service with data {} ", currencyRequest);
        Instant startTime = now();
        Response<CurrencyConversionDto> conversionResponse = mobCommonClient.convertBetweenCurrencies(currencyRequest);
        if (TRUE.equalsIgnoreCase(conversionResponse.getHasError())) {
            final String errorDetails = getErrorDetails(conversionResponse);
            log.error("[MobCommonService] Exception in calling mob customer for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);
        }
        log.info("[MobCommonService] Currency Conversion success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return conversionResponse.getData();
    }

    public CustomerDetailsDto getCustomerDetails(final String cif, final String channelTraceId) {
        log.info("[MobCommonService] calling customer service client for getting customer details");

        Response<com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto> response = mobCommonClient.getCustomerDetails(cif, channelTraceId);
        if (TRUE.equalsIgnoreCase(response.getHasError()) || StringUtils.isNotBlank(response.getErrorCode())) {
            log.error("Error while calling mob common for customer detail {} {} ", response.getErrorCode(), response.getErrorMessage());
            GenericExceptionHandler.handleError(TransferErrorCode.EXTERNAL_SERVICE_ERROR, TransferErrorCode.EXTERNAL_SERVICE_ERROR.getErrorMessage(), ErrorUtils.getErrorDetails(response));
        }
        return mobCommonClient.getCustomerDetails(cif, channelTraceId).getData();

    }
}
