package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@FeignClient(name = "bankcharges", url = "${app.services.bank-charges}", configuration = FeignConfig.class)
public interface BankChargesServiceClient {

	@GetMapping("/api/v1/charges/transaction/{accountClass}")
	Response<TransactionChargesDto> getTransactionCharges(@RequestHeader Map<String, String> headerMap, @PathVariable("accountClass") String accountClass, @RequestParam String transactionCurrency);

}
