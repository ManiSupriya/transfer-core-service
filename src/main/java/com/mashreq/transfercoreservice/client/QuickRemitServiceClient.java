package com.mashreq.transfercoreservice.client;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mashreq.transfercoreservice.client.dto.QRExchangeRequest;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;


@FeignClient(name = "quickremit", url = "${app.services.quick-remit}", configuration = FeignConfig.class)
public interface QuickRemitServiceClient {

	@PostMapping(value = "/api/v1/quickremit/exchange")
	Response<QRExchangeResponse> exchange(@Valid @RequestBody QRExchangeRequest qrExchangeRequest);

}
