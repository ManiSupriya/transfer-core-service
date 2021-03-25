package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.MobRedisService;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CountryDto;
import com.mashreq.transfercoreservice.client.dto.CountryResponseDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealConversionRateRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealConversionRateResponseDto;
import com.mashreq.transfercoreservice.model.ApplicationSettingDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR;
import static java.time.Instant.now;
import static java.util.Objects.isNull;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobCommonService {

    private final MobCommonClient mobCommonClient;
    private final MobRedisService mobRedisService;
    public static final String MOB_AE_ROUTING_CODE_SUPPORTED_COUNTRIES = "MOB:AE:ROUTING_CODE_SUPPORTED_COUNTRIES";
    public static final String TRUE = "true";

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
    
    public DealConversionRateResponseDto getConvertBetweenCurrenciesWithDeal(DealConversionRateRequestDto dealConversionRateRequestDto) {
        log.info("[MobCommonService] Calling deal currency conversion service with data {} ", dealConversionRateRequestDto);
        Instant startTime = now();
        Response<DealConversionRateResponseDto> conversionResponse = mobCommonClient.convertBetweenCurrenciesWithDeal(dealConversionRateRequestDto);
        if (ResponseStatus.ERROR == conversionResponse.getStatus() || isNull(conversionResponse.getData())) {
            final String errorDetails = getErrorDetails(conversionResponse);
            log.error("[MobCommonService] Exception in calling mob customer for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(conversionResponse));
        }
        log.info("[MobCommonService] Currency Deal Conversion success in  {} ms ", Duration.between(startTime, now()).toMillis());
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

    public List<ApplicationSettingDto> getApplicationSettings(String group) {
        log.info("[MobCommonService] Calling MobCommonService to get application settings for group = {}", group);
        Instant startTime = now();
        Response<List<ApplicationSettingDto>>  response = mobCommonClient.getApplicationSettings(group);
        if (Objects.isNull(response.getData()) || ResponseStatus.ERROR == response.getStatus()) {
            log.error("Error while fetching application settings. Group = {} ", group);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(response));
        }
        log.info("[MobCommonService] MobCommonService response success in nanoseconds {} ", Duration.between(startTime, now()));
        return response.getData();
    }

    public List<CountryDto> getRoutingCodeEnabledCountries() {
        log.info("[MobCommonService] Calling MobCommonClient to get routing code enabled countries");
        Instant startTime = now();
        CountryResponseDto countryResponseDto = mobRedisService.get(MOB_AE_ROUTING_CODE_SUPPORTED_COUNTRIES, CountryResponseDto.class);

        if (Objects.nonNull(countryResponseDto) && CollectionUtils.isNotEmpty((countryResponseDto.getAllCountries()))) {
            log.info("CACHE HIT for getRoutingCodeCountries");
            return countryResponseDto.getAllCountries();
        } else {
            log.info("CACHE MISS for getRoutingCodeCountries");
            Response<CountryResponseDto> response = mobCommonClient.getRoutingCodeEnabledCountries();
            if (Objects.isNull(response.getData()) || response.getStatus().equals(ResponseStatus.ERROR)) {
                GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                        getErrorDetails(response));
            }
            log.info("[MobCommonService] MobCommonService response success in nanoseconds {} ", Duration.between(startTime, now()));
            return response.getData().getAllCountries();
        }
    }
}
