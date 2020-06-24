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
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR;
import static java.time.Instant.now;
import static java.util.Objects.isNull;

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

        Response<LimitValidatorResultsDto> response =
                mobCommonClient.validateAvailableLimit(cifId, beneficiaryTypeCode, amount);

        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            final String errorDetails = getErrorDetails(response);
            log.error("[MobCommonService] Exception in calling mob customer for limit validation ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(response));
        }
        log.info("[MobCommonService] Limit validation response success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return response.getData();
    }

    public Set<MoneyTransferPurposeDto> getPaymentPurposes( String transactionType, String qrType, String accountType) {
        log.info("[MobCommonService] Calling MobCommonService for getting POP for QR transfer to country={}  ",
                qrType);
        Instant startTime = now();
        final Response<Set<MoneyTransferPurposeDto>> paymentPurpose = mobCommonClient.getPaymentPurpose( transactionType, qrType, accountType);

        if (ResponseStatus.ERROR == paymentPurpose.getStatus() || isNull(paymentPurpose.getData())) {
            final String errorDetails = getErrorDetails(paymentPurpose);
            log.error("[MobCommonService] Exception in calling mob customer for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(paymentPurpose));
        }
        log.info("[MobCommonService] Payment purpose response success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return paymentPurpose.getData();
    }

    public CurrencyConversionDto getConvertBetweenCurrencies(CoreCurrencyConversionRequestDto currencyRequest) {
        log.info("[MobCommonService] Calling currency conversion service with data {} ", currencyRequest);
        Instant startTime = now();
        Response<CurrencyConversionDto> conversionResponse = mobCommonClient.convertBetweenCurrencies(currencyRequest);
        if (ResponseStatus.ERROR == conversionResponse.getStatus() || isNull(conversionResponse.getData())) {
            final String errorDetails = getErrorDetails(conversionResponse);
            log.error("[MobCommonService] Exception in calling mob customer for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(conversionResponse));
        }
        log.info("[MobCommonService] Currency Conversion success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return conversionResponse.getData();
    }

    public CustomerDetailsDto getCustomerDetails(final String cif) {
        log.info("[MobCommonService] calling customer service client for getting customer details");

        Response<com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto> response = mobCommonClient.getCustomerDetails(cif);
        if (ResponseStatus.ERROR == response.getStatus() || isNull(response.getData())) {
            log.error("Error while calling mob common for customer detail {} {} ", response.getErrorCode(), response.getMessage());
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(response));
        }
        return mobCommonClient.getCustomerDetails(cif).getData();

    }
}