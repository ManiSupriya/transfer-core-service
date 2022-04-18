package com.mashreq.transfercoreservice.api;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/twoFactorAuthenticaton")
@Api(value = "Two factor authentication")
public class TwoFactorAuthenticationController {
	private final TwoFactorAuthRequiredCheckService service;

	@ApiOperation(value = "For validating service", response = TwoFactorAuthRequiredCheckRequestDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping("/isRequired")
    public Response checkIfTwoFactorAuthenticationRequired(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
                                  @Valid @RequestBody TwoFactorAuthRequiredCheckRequestDto request) {
        log.info("TwoFactorAuthentication verification request received ");
        log.info("Fund transfer meta data created {} ", htmlEscape(metaData));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(service.checkIfTwoFactorAuthenticationRequired(metaData, request)).build();
    }
}
