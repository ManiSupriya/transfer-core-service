package com.mashreq.transfercoreservice.api;

import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
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
                                  @RequestHeader("X-CHANNEL-HOST") String channelHost,
                                  @RequestHeader(HeaderNames.CHANNEL_TYPE_HEADER_NAME) String channelName,
                                  @RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
                                  @RequestHeader(HeaderNames.X_USSM_USER_REDIS_KEY) final String userCacheKey,
                                  @RequestHeader(HeaderNames.X_CORRELATION_ID) final String correlationId,
                                  @RequestHeader(HeaderNames.X_USSM_USER_NAME) final String userId,
                                  @Valid @RequestBody FundTransferRequestDTO request) {

        log.info("{} Fund transfer for request received ", request.getServiceType());
        RequestMetaData metadata = RequestMetaData.builder()
                .channel(channelName)
                .channelTraceId(channelTraceId)
                .channelHost(channelHost)
                .userCacheKey(userCacheKey)
                .username(userId)
                .coRelationId(correlationId)
                .primaryCif(cifId)
                .build();

        log.info("Fund transfer meta data created {} ", metadata);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(fundTransferService.transferFund(metadata, request)).build();
    }

}
