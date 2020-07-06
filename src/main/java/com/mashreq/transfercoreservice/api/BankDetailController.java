package com.mashreq.transfercoreservice.api;


import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.banksearch.BankDetailRequestDto;
import com.mashreq.transfercoreservice.banksearch.BankDetailService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/bank/search")
public class BankDetailController {

    private final BankDetailService bankDetailService;
    private static final String X_CHANNEL_TRACE_ID = "X-CHANNEL-TRACE-ID";

    @PostMapping
    public Response getBankDetails(@RequestAttribute(X_CHANNEL_TRACE_ID) String channelTraceId,
                                   @RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
                                   @Valid @RequestBody BankDetailRequestDto bankDetailRequest) {
        log.info("Received request to search {} with value {} ", bankDetailRequest.getType(), bankDetailRequest.getValue());
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(bankDetailService.getBankDetails(channelTraceId, bankDetailRequest, metaData)).build();
    }

    @GetMapping("/ifsc/{code}")
    public Response getIfscCodeDetails(@RequestAttribute(X_CHANNEL_TRACE_ID) String channelTraceId,
                                       @RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
                                       @PathVariable final String code) {
        log.info("Received request to search ifsc-code with value {} ", code);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(bankDetailService.getBankDeatilsByIfsc(channelTraceId, code, metaData)).build();
    }
}
