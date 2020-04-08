package com.mashreq.transfercoreservice.client.mobcommon;

import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@FeignClient(name = "mobcommon", url = "${app.services.mobcommon}", configuration = FeignConfig.class)
public interface MobCommonClient {

    @GetMapping("/v1/limit/available/{beneficiaryTypeCode}")
    Response<LimitValidatorResultsDto> validateAvailableLimit(@RequestHeader("X-CIF-ID") String cifId,
                                                              @NotNull @PathVariable String beneficiaryTypeCode,
                                                              @RequestParam(value = "amount", required = false) BigDecimal amount);

}
