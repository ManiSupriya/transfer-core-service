package com.mashreq.transfercoreservice.client;

import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryModificationValidationRequest;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryModificationValidationResponse;
import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.fundtransfer.dto.AdditionalFields;
import com.mashreq.webcore.dto.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * @author shahbazkh
 * @date 3/16/20
 */

@FeignClient(name = "beneficiary", url = "${app.services.beneficiary}", configuration = FeignConfig.class)
public interface BeneficiaryClient {

    @GetMapping("/v1/beneficiary/{id}")
    Response<BeneficiaryDto> getByIdWithoutValidation(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId, @NotNull @PathVariable Long id);
    
    @GetMapping("/v2/beneficiary/{id}")
    Response<BeneficiaryDto> getByIdWithoutValidationV2(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId, @NotNull @PathVariable Long id);

    @PutMapping("/v1/beneficiary/{validationType}/internal/{benId}")
    Response<BeneficiaryDto> update(@RequestBody AdditionalFields additionalFields, @PathVariable Long benId, @PathVariable String validationType);
    
    @GetMapping("/v1/charity-beneficiary/{id}")
    Response<CharityBeneficiaryDto> getCharity(@NotNull @PathVariable String id);
    
    @GetMapping("/v1/beneficiary/{validation-type}/internal/{id}")
    Response<BeneficiaryDto> getById(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId, @PathVariable(value = "validation-type") String validationType, @NotNull @PathVariable(value = "id") Long id);
    
    @GetMapping("/v2/beneficiary/{validation-type}/internal/{id}")
    Response<BeneficiaryDto> getByIdV2(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId, @PathVariable(value = "validation-type") String validationType, @NotNull @PathVariable(value = "id") Long id);

    @PostMapping("/v1/beneficiary/isRecentlyModified")
    Response<BeneficiaryModificationValidationResponse> isRecentlyUpdated(@RequestHeader(HeaderNames.CIF_HEADER_NAME) String cifId, BeneficiaryModificationValidationRequest request);
}
