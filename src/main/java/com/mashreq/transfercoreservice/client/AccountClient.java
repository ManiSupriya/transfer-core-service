package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CifProductsDto;
import com.mashreq.transfercoreservice.config.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FeignClient(name = "accounts", url = "${app.services.accounts}", configuration = FeignConfig.class)
public interface AccountClient {

    @GetMapping(value = "/api/accounts/cif/{cifId}")
    Response<CifProductsDto> searchAccounts(@PathVariable("cifId") String cifId, @RequestParam(required = false) List<String> linkedCifs);
}
