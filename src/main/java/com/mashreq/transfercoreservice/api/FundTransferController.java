package com.mashreq.transfercoreservice.api;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @ApiOperation(value = "Processes to start payment", response = FundTransferRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    public Response transferFunds(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
                                  @Valid @RequestBody FundTransferRequestDTO request) {

        log.info("{} Fund transfer for request received ", htmlEscape(request.getServiceType()));
        log.info("Fund transfer meta data created {} ", htmlEscape(metaData));
        if(request.getAmount() == null && request.getSrcAmount() ==null){
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_REQUEST, "Bad Request", "Both debitAmount and credit amount are missing");
        }
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(fundTransferService.transferFund(metaData, request)).build();
    }

}
