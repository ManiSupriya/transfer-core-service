package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "bankcharges", url = "${app.services.bank-charges}")
public interface BankChargesServiceClient {

	@GetMapping("/api/v1/charges/transaction/{accountClass}")
	Response<TransactionChargesDto> getTransactionCharges(@PathVariable("accountClass") String accountClass, @RequestParam String transactionCurrency);

}
