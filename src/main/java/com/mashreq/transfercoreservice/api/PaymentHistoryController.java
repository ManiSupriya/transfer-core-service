package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.service.PaymentHistoryService;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Response transferFunds(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                                  @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                                  @RequestAttribute("X-CHANNEL-NAME") String channelName,
                                  @RequestHeader("X-CIF-ID") final String cifId,
                                  @NotNull @PathVariable("serviceType") final  String serviceType) {

        return Response.builder()
                .data(paymentHistoryService.getCharityPaid(cifId, serviceType))
                .build();
    }
}