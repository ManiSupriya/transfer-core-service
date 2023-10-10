package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/twoFactorAuthentication")
@Tag(name = "Two factor authentication")
public class TwoFactorAuthenticationController {
    private final TwoFactorAuthRequiredCheckService service;

    @Operation(summary = "For validating service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
    })
    @PostMapping("/isRequired")
    public Response<TwoFactorAuthRequiredCheckResponseDto> checkIfTwoFactorAuthenticationRequired
            (@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
             @Valid @RequestBody TwoFactorAuthRequiredCheckRequestDto request) {
        log.info("TwoFactorAuthentication verification request received ");
        log.info("Fund transfer meta data created {} ", htmlEscape(metaData));
        return Response.<TwoFactorAuthRequiredCheckResponseDto>builder()
                .status(ResponseStatus.SUCCESS)
                .data(service.checkIfTwoFactorAuthenticationRequired(metaData, request)).build();
    }
}
