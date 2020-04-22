package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.client.dto.CustomerDetailsDto;
import com.mashreq.transfercoreservice.config.feign.FeignAccessTokenInterceptor;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@FeignClient(
        name = "customers",
        url = "${app.services.customers}",
        configuration = FeignAccessTokenInterceptor.class)
public interface CustomerClient {

    @GetMapping("/api/customers/cif/{cif}/profile")
    Response<CustomerDetailsDto> getCustomerProfile(@PathVariable(value = "cif") String cif) ;

}
