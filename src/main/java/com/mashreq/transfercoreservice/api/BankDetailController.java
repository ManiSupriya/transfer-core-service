package com.mashreq.transfercoreservice.api;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.banksearch.BankDetailRequestDto;
import com.mashreq.transfercoreservice.banksearch.BankDetailService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

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

    @PostMapping
    public Response getBankDetails(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData metaData,
                                   @Valid @RequestBody BankDetailRequestDto bankDetailRequest) {
        log.info("Received request to search {} with value {} ", htmlEscape(bankDetailRequest.getType()), htmlEscape(bankDetailRequest.getValue()));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(bankDetailService.getBankDetails(metaData.getChannelTraceId(), bankDetailRequest, metaData)).build();
    }

    @GetMapping("/ifsc/{code}")
    public Response getIfscCodeDetails(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData metaData,
                                       @PathVariable final String code) {
        log.info("Received request to search ifsc-code with value {} ", htmlEscape(code));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(bankDetailService.getBankDeatilsByIfsc(metaData.getChannelTraceId(), code, metaData)).build();
    }
}
