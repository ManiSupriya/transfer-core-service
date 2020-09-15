package com.mashreq.transfercoreservice.api;


import static org.springframework.web.util.HtmlUtils.htmlEscape;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.banksearch.BankDetailRequestDto;
import com.mashreq.transfercoreservice.banksearch.BankDetailService;
import com.mashreq.transfercoreservice.banksearch.SwiftBankDetailRequestDto;
import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final AccountClient accountClient;

    
    @PostMapping("/dblink")
    public Response getSwiftBankDetails(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetadata,
                                   @Valid @RequestBody SwiftBankDetailRequestDto bankDetailRequest) {
        log.info("Received request to search bank detail for swift code {} ", htmlEscape(bankDetailRequest.getSwiftCode()));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(bankDetailService.getBankDetails(bankDetailRequest.getSwiftCode(), requestMetadata)).build();
    }
    
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
