package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealEnquiryDto;
import com.mashreq.webcore.dto.response.Response;

import io.swagger.annotations.ApiParam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import javax.validation.constraints.NotEmpty;

/**
 * Feign Client
 */
@FeignClient(value = "maintenance",
        url = "${app.services.maintenance}",
        configuration = FeignConfig.class)
public interface MaintenanceClient {

    @GetMapping("/api/currencies/exchange-rates/conversion")
    Response<CurrencyConversionDto> convertBetweenCurrencies(@SpringQueryMap CoreCurrencyConversionRequestDto dto);

    @GetMapping("/v2/api/countries")
    Response<List<CountryMasterDto>> getAllCountries(@RequestParam final String channel, @RequestParam final String region,
                                                     @RequestParam final Boolean active);
    
    @GetMapping("/api/deal-enquiry/{dealNumber}")
    public ResponseEntity<Response<DealEnquiryDto>> getFXDealInformation( @ApiParam("FX Deal Number")  @NotEmpty @PathVariable("dealNumber") String dealNumber);




}
