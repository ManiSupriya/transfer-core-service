package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.config.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign Client
 */
@FeignClient(value = "maintenance",
        url = "${app.services.maintenance}",
        configuration = FeignConfig.class)
public interface MaintenanceClient {

    @GetMapping("/api/currencies/exchange-rates/conversion")
    Response<CurrencyConversionDto> convertBetweenCurrencies(@SpringQueryMap CoreCurrencyConversionRequestDto dto);
}
