package com.mashreq.transfercoreservice.client.mobcommon;

import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotNull;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.model.ApplicationSettingDto;
import com.mashreq.transfercoreservice.promo.dto.PromoCodeTransactionRequestDto;
import com.mashreq.webcore.dto.response.Response;

@FeignClient(name = "mobcommon", url = "${app.services.mobcommon}", configuration = FeignConfig.class)
public interface MobCommonClient {

    @GetMapping("/v1/payment-purposes/{transactionType}")
    Response<Set<MoneyTransferPurposeDto>> getPaymentPurpose(@NotNull @PathVariable String transactionType,
                                                             @RequestParam(value = "qrType") String qrType,
                                                             @RequestParam(value = "customerType") String customerType);
    @GetMapping("/v1/countries/ROUTING_CODE_ENABLED")
    Response<CountryResponseDto> getRoutingCodeEnabledCountries();
    
    @GetMapping("/v1/countries/ALL")
    Response<CountryResponseDto> getAllCountries();

    @PostMapping("/v1/currency/conversion")
    Response<CurrencyConversionDto> convertBetweenCurrencies(@RequestBody CoreCurrencyConversionRequestDto conversionRateRequestDto);

    @GetMapping("/v1/currency")
	Response<List<CoreCurrencyDto>> getTransferCurrencies(@RequestParam(value = "function") String function,
			@RequestHeader(HeaderNames.COUNTRY_HEADER_NAME) String country,
			@RequestParam(value = "currencyCode") String currencyCode);

    @GetMapping("/v1/customer")
    Response<CustomerDetailsDto> getCustomerDetails(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId);

    @PostMapping("/v1/currency/dealConversion")
    Response<DealConversionRateResponseDto> convertBetweenCurrenciesWithDeal(@RequestBody DealConversionRateRequestDto conversionRateRequestDto);

    @GetMapping(value = "/v1/settings")
    Response<List<ApplicationSettingDto>> getApplicationSettings(@RequestParam(value = "group", required = false) final String group);
    
    @PostMapping(value = "/v1/promo/validate/{promoCode}", consumes = "application/json")
    Response<Void> validatePromo(@NotNull @PathVariable String promoCode, @RequestBody PromoCodeTransactionRequestDto promoCodeReq);

    @GetMapping(value = "/v1/account/debit/freeze/{accountNumber}")
    Response<Boolean> checkDebitFreeze(
            @RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId,
            @RequestHeader(HeaderNames.X_USSM_USER_REDIS_KEY) String userCacheKey,
            @NotNull @PathVariable String accountNumber);

    @GetMapping(value = "/v1/account/credit/freeze/{accountNumber}")
    Response<Boolean> checkCreditFreeze(
            @RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId,
            @RequestHeader(HeaderNames.X_USSM_USER_REDIS_KEY) String userCacheKey,
            @RequestParam ServiceType serviceTypeCode,
            @NotNull @PathVariable String accountNumber);

    @GetMapping("/v1/countries/{countryCode}")
    Response<CountryDto> getCountryValidationRule(@PathVariable String countryCode);

    @PostMapping("/v2/limit/available")
    Response<LimitValidatorResponse> getAvailableLimits(@RequestBody LimitValidatorRequest limitValidatorRequest);
}
