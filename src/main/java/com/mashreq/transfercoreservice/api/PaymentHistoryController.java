package com.mashreq.transfercoreservice.api;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.paymenthistory.PaymentHistoryService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.regexp.RE;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/v1/payment-history")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    @ApiOperation(value = "Processes to start payment", response = FundTransferRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @GetMapping("/charity-paid/{serviceType}")
    public Response transferFunds(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData,
                                  @RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
                                  @NotNull @PathVariable("serviceType") final String serviceType) {

        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(paymentHistoryService.getCharityPaid(cifId, serviceType))
                .build();
    }
}
