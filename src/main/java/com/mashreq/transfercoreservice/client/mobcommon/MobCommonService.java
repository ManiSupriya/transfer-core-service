package com.mashreq.transfercoreservice.client.mobcommon;

import static com.mashreq.transfercoreservice.client.ErrorUtils.getErrorDetails;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_DEBIT_FREEZE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CREDIT_FREEZE;
import static java.time.Instant.now;
import static java.util.Objects.isNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mashreq.mobcommons.services.CustomHtmlEscapeUtil;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.cache.MobRedisService;
import com.mashreq.transfercoreservice.client.dto.CountryDto;
import com.mashreq.transfercoreservice.client.dto.CountryResponseDto;
import com.mashreq.transfercoreservice.client.dto.TransferSupportedCountryDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealConversionRateRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealConversionRateResponseDto;
import com.mashreq.transfercoreservice.model.ApplicationSettingDto;
import com.mashreq.transfercoreservice.promo.dto.PromoCodeTransactionRequestDto;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobCommonService {

    private final MobCommonClient mobCommonClient;
    private final MobRedisService mobRedisService;
    public static final String MOB_AE_ROUTING_CODE_SUPPORTED_COUNTRIES = "MOB:AE:ROUTING_CODE_SUPPORTED_COUNTRIES";
    public static final String MOB_AE_FILTERED_COUNTRIES = "MOB:AE:FILTERED_COUNTRIES";
    public static final String MOB_AE_TRANSFER_SUPPORTED_COUNTRIES = "MOB:AE:TRANSFER_SUPPORTED_COUNTRIES";

    private static final TypeReference<Map<String, CountryDto>> FILTERED_COUNTRIES_TYPE = new TypeReference<Map<String, CountryDto>>() {
    };
    private static final TypeReference<List<TransferSupportedCountryDto>> TRANSFER_SUPPORTED_COUNTRIES_TYPE = new TypeReference<List<TransferSupportedCountryDto>>() {
    };
    public static final String TRUE = "true";

    public Set<MoneyTransferPurposeDto> getPaymentPurposes( String transactionType, String qrType, String accountType) {
        log.info("[MobCommonService] Calling MobCommonService for getting POP for QR transfer to country={}  ",
                qrType);
        Instant startTime = now();
        final Response<Set<MoneyTransferPurposeDto>> paymentPurpose = mobCommonClient.getPaymentPurpose( transactionType, qrType, accountType);

        if (ResponseStatus.ERROR == paymentPurpose.getStatus() || isNull(paymentPurpose.getData())) {
            final String errorDetails = getErrorDetails(paymentPurpose);
            log.error("[MobCommonService] Exception in calling mob common for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(paymentPurpose));
        }
        log.info("[MobCommonService] Payment purpose response success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return paymentPurpose.getData();
    }
    
    public DealConversionRateResponseDto getConvertBetweenCurrenciesWithDeal(DealConversionRateRequestDto dealConversionRateRequestDto) {
        log.info("[MobCommonService] Calling deal currency conversion service with data {} ", dealConversionRateRequestDto);
        Instant startTime = now();
        Response<DealConversionRateResponseDto> conversionResponse = mobCommonClient.convertBetweenCurrenciesWithDeal(dealConversionRateRequestDto);
        if (ResponseStatus.ERROR == conversionResponse.getStatus() || isNull(conversionResponse.getData())) {
            final String errorDetails = getErrorDetails(conversionResponse);
            log.error("[MobCommonService] Exception in calling mob common for POP ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), getErrorDetails(conversionResponse));
        }
        log.info("[MobCommonService] Currency Deal Conversion success in  {} ms ", Duration.between(startTime, now()).toMillis());
        return conversionResponse.getData();
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
    
    public Map<String, String> getCountryCodeMap() {
        log.info("[MobCommonService] Calling MobCommonClient to get all countries");
        Instant startTime = now();
        List<TransferSupportedCountryDto> countryResponseDto = mobRedisService.get(MOB_AE_TRANSFER_SUPPORTED_COUNTRIES, TRANSFER_SUPPORTED_COUNTRIES_TYPE);

        if (Objects.isNull(countryResponseDto) || Objects.nonNull(countryResponseDto) && CollectionUtils.isNotEmpty(countryResponseDto)) {
            log.info("CACHE MISS for getAllCountries");
            Response<CountryResponseDto> response = mobCommonClient.getAllCountries();
            if (Objects.isNull(response.getData()) || response.getStatus().equals(ResponseStatus.ERROR)) {
                GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                        getErrorDetails(response));
            }
            log.info("[MobCommonService] MobCommonService response success in nanoseconds {} ", Duration.between(startTime, now()));
           return response.getData().getAllCountries().stream()
           		.collect(Collectors.toMap(CountryDto::getCode, CountryDto::getName));
        }
        
        return countryResponseDto.stream()
        		.collect(Collectors.toMap(TransferSupportedCountryDto::getCode, TransferSupportedCountryDto::getName));
    }
    
    public void validatePromoCode(PromoCodeTransactionRequestDto promoCodeReq) {
        log.info("[MobCommonService] Calling MobCommonClient to validate promo code");
        
        Response<Void> response = mobCommonClient.validatePromo(promoCodeReq.getPromoCode(), promoCodeReq);
        if (response.getStatus().equals(ResponseStatus.ERROR)) {
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getErrorMessage(),
                    getErrorDetails(response));
        }
        log.info("[MobCommonService] MobCommonService promo code validation response success");
    }

    public void checkDebitFreeze(RequestMetaData requestMetaData, String accountNumber) {
        log.info("[MobCommonService] Calling MobCommonService for checking debit freeze for accountNumber {}  ", CustomHtmlEscapeUtil.htmlEscape(accountNumber));
        Instant startTime = now();
        final Response<Boolean> result = mobCommonClient.checkDebitFreeze(requestMetaData.getPrimaryCif(), requestMetaData.getUserCacheKey(), accountNumber);

        if (ResponseStatus.ERROR == result.getStatus() || (isNull(result.getData()))) {
            final String errorDetails = getErrorDetails(result);
            log.error("[MobCommonService] Exception in calling mob common for debit freeze ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);
        }

        if(result.getData()){
            log.error("[MobCommonService] accountNumber {} is debit freeze ", CustomHtmlEscapeUtil.htmlEscape(accountNumber));
            GenericExceptionHandler.handleError(ACCOUNT_DEBIT_FREEZE,
                    ACCOUNT_DEBIT_FREEZE.getErrorMessage());
        }

        log.info("[MobCommonService] Payment purpose response success in  {} ms ", Duration.between(startTime, now()).toMillis());
    }

    public void checkCreditFreeze(RequestMetaData requestMetaData, ServiceType serviceType, String accountNumber) {
        log.info("[MobCommonService] Calling MobCommonService for checking credit freeze for accountNumber {}  ", CustomHtmlEscapeUtil.htmlEscape(accountNumber));
        Instant startTime = now();
        final Response<Boolean> result = mobCommonClient.checkCreditFreeze(
                requestMetaData.getPrimaryCif(),
                requestMetaData.getUserCacheKey(),
                serviceType,
                accountNumber);

        if (ResponseStatus.ERROR == result.getStatus() || (isNull(result.getData()))) {
            final String errorDetails = getErrorDetails(result);
            log.error("[MobCommonService] Exception in calling mob common for credit freeze ={} ", errorDetails);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR,
                    EXTERNAL_SERVICE_ERROR.getErrorMessage(), errorDetails);
        }
        if(result.getData()){
            log.error("[MobCommonService] accountNumber {} is credit freeze ", CustomHtmlEscapeUtil.htmlEscape(accountNumber));
            GenericExceptionHandler.handleError(ACCOUNT_CREDIT_FREEZE,
                    ACCOUNT_CREDIT_FREEZE.getErrorMessage());
        }
        log.info("[MobCommonService] Payment purpose response success in  {} ms ", Duration.between(startTime, now()).toMillis());
    }

    public CountryDto getCountryValidationRules(String countryCode) {
        log.info("[MobCommonService] Calling MobCommonService to get filtered country validation rules");
        Instant startTime = now();
        final Map<String, CountryDto> cache = mobRedisService.get(MOB_AE_FILTERED_COUNTRIES, FILTERED_COUNTRIES_TYPE);
        if (null != cache && !cache.isEmpty()) {
            log.info("CACHE HIT for getCountryValidationRules");
            return cache.get(countryCode);

        } else {
            log.info("CACHE MISS for getCountryValidationRules");
            Response<CountryDto> response = mobCommonClient.getCountryValidationRule(countryCode);
            if (Objects.isNull(response.getData()) || ResponseStatus.ERROR == response.getStatus()) {
                log.error("Error while fetching filtered country validation rules");
                GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR, EXTERNAL_SERVICE_ERROR.getCustomErrorCode(), getErrorDetails(response));
            }
            log.info("[MobCommonService] MobCommonService response success in nanoseconds {} ", Duration.between(startTime, now()));
            return response.getData();
        }
    }
}
