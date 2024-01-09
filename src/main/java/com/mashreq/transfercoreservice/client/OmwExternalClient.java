package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.UaeIbanTitleFetchRequest;
import com.mashreq.transfercoreservice.client.dto.UaeIbanTitleFetchResponse;
import com.mashreq.transfercoreservice.config.feign.OmwExternalFeignConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@FeignClient(name = "omwexternal", url = "${app.services.omw-external.url}", configuration = OmwExternalFeignConfig.class)
public interface OmwExternalClient {

        @PostMapping(value = "/cbms/iban-details")
        UaeIbanTitleFetchResponse getAccountTitle(@Valid @RequestBody UaeIbanTitleFetchRequest uaeIbanTitleFetchRequest, @RequestParam String serviceId);
}
