package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.*;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * Feign Client
 */
@FeignClient(value = "maintenance",
        url = "${app.services.maintenance}",
        configuration = FeignConfig.class)
public interface MaintenanceClient {

    @GetMapping("/api/currencies/exchange-rates/conversion")
    Response<CurrencyConversionDto> convertBetweenCurrencies(@SpringQueryMap CoreCurrencyConversionRequestDto dto);

    @GetMapping("/api/transfer/purposes")
    Response<Set<PurposeOfTransferDto>> getAllPurposeCodes(@RequestParam(value="transactionType") final String transactionType);

    @GetMapping("/v2/api/countries")
    Response<List<CountryMasterDto>> getAllCountries(@RequestParam final String channel, @RequestParam final String region,
                                                     @RequestParam final Boolean active);




}
