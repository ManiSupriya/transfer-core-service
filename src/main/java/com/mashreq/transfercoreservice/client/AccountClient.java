package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CifProductsDto;
import com.mashreq.transfercoreservice.config.FeignConfig;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FeignClient(name = "accounts", url = "${app.services.accounts}", configuration = FeignConfig.class)
public interface AccountClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/accounts/cif/{cifId}")
    Response<CifProductsDto> searchAccounts(@PathVariable("cifId") String cifId, @RequestParam(required = false) List<String> linkedCifs);
}
