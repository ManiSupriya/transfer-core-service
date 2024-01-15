package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.UaeIbanTitleFetchRequest;
import com.mashreq.transfercoreservice.client.dto.UaeIbanTitleFetchResponse;
import com.mashreq.transfercoreservice.config.feign.OmwExternalFeignConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@FeignClient(name = "omwexternal", url = "${app.services.omw-external.url}", configuration = OmwExternalFeignConfig.class)
public interface OmwExternalClient {

        @PostMapping(value = "/cbms/iban-details",produces = APPLICATION_JSON,consumes = APPLICATION_JSON)
        UaeIbanTitleFetchResponse getAccountTitle(@Valid @RequestBody UaeIbanTitleFetchRequest uaeIbanTitleFetchRequest, @RequestParam String serviceId);
}
