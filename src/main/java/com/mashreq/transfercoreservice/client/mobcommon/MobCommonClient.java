package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.common.HeaderNames;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;

@FeignClient(name = "mobcommon", url = "${app.services.mobcommon}", configuration = FeignConfig.class)
public interface MobCommonClient {

    @GetMapping("/v1/limit/available/{beneficiaryTypeCode}")
    Response<LimitValidatorResultsDto> validateAvailableLimit(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId,
                                                              @NotNull @PathVariable String beneficiaryTypeCode,
                                                              @RequestParam(value = "amount", required = false) BigDecimal amount);


    @GetMapping("/v1/payment-purposes/{transactionType}")
    Response<Set<MoneyTransferPurposeDto>> getPaymentPurpose(@NotNull @PathVariable String transactionType,
                                                             @RequestParam(value = "qrType") String qrType,
                                                             @RequestParam(value = "customerType") String customerType);


    @PostMapping("/v1/currency/conversion")
    Response<CurrencyConversionDto> convertBetweenCurrencies(@RequestBody CoreCurrencyConversionRequestDto conversionRateRequestDto);


    @GetMapping("/v1/customer")
    Response<CustomerDetailsDto> getCustomerDetails(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId);


}
