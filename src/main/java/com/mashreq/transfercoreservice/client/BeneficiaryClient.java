//package com.mashreq.transfercoreservice.client;
//
//import com.mashreq.transfercoreservice.config.FeignConfig;
//import com.mashreq.webcore.dto.response.Response;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestHeader;
//
//import javax.validation.constraints.NotNull;
//
///**
// * @author shahbazkh
// * @date 3/16/20
// */
//
//@FeignClient(name = "beneficiary", url = "${app.services.beneficiary}", configuration = FeignConfig.class)
//public interface BeneficiaryClient {
//
//    @GetMapping("/{id}")
//     Response<BeneficiaryDto> getBydId(String cifId, @NotNull @PathVariable Long id);
//}
