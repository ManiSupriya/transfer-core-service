package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.BICCodeSearchRequestDto;
import com.mashreq.transfercoreservice.client.dto.BICCodeSearchResponseDto;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "beneficiaryservice", url = "${app.services.beneficiaryservice}")
public interface BeneficiaryServiceClient {

    @PostMapping("/api/v1/bankdetails/accuitySearch")
    Response<List<BICCodeSearchResponseDto>> fetchBankDetailsWithBic(BICCodeSearchRequestDto requestDto);
}
