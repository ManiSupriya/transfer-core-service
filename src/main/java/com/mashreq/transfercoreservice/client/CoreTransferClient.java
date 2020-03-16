package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CoreFundTransferRequestDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferMWResponse;
import com.mashreq.transfercoreservice.config.FeignConfig;
import com.mashreq.transfercoreservice.errors.FundTransferException;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "transfer",
        configuration = FeignConfig.class,
        url = "${app.services.accounts}")
public interface CoreTransferClient {

    @PostMapping("api/accounts/transfer")
    Response<FundTransferMWResponse> transferFundsBetweenAccounts(@RequestBody CoreFundTransferRequestDto fundTransferDto) throws FundTransferException;
}
