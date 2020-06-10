package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
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
    public Response transferFunds(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                                  @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                                  @RequestAttribute("X-CHANNEL-NAME") String channelName,
                                  @RequestHeader("X-CIF-ID") final String cifId,
                                  @Valid @RequestBody FundTransferRequestDTO request) {
        log.info("{} Fund transfer for request received ", request.getServiceType());
        FundTransferMetadata metadata = FundTransferMetadata.builder()
                .channel(channelName)
                .channelTraceId(channelTraceId)
                .channelHost(channelHost)
                .primaryCif(cifId)
                .build();

        log.info("Fund transfer meta data created {} ", metadata);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(fundTransferService.transferFund(metadata, request)).build();
    }
}
