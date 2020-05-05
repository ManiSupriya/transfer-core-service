package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.service.FlexRuleEngineService;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author shahbazkh
 * @date 4/21/20
 */


@Slf4j
@RestController
@RequestMapping("/v1/rule-engine")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class FlexRuleEngineController {

    private final FlexRuleEngineService flexRuleEngineService;

    @ApiOperation(value = "Fetch Rules for flex engine", response = FlexRuleEngineResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    public Response fetchRule(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                              @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                              @RequestAttribute("X-CHANNEL-NAME") String channelName,
                              @RequestHeader("X-CIF-ID") final String cifId,
                              @Valid @RequestBody FlexRuleEngineRequestDTO request) {
        log.info("{} Flex Rule engine transfer for request received ", request);

        FlexRuleEngineMetadata metadata = FlexRuleEngineMetadata.builder()
                .channelTraceId(channelTraceId)
                .cifId(cifId)
                .build();

        return Response.builder().data(flexRuleEngineService.getRules(metadata, request)).build();

    }

    @ApiOperation(value = "Fetch Rules for flex engine", response = ChargeResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping("/charges")
    public Response fetchCharges(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                                 @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                                 @RequestAttribute("X-CHANNEL-NAME") String channelName,
                                 @RequestHeader("X-CIF-ID") final String cifId,
                                 @Valid @RequestBody ChargesRequestDTO request) {
        log.info("{} Flex Rule Charge request received ", request);

        FlexRuleEngineMetadata metadata = FlexRuleEngineMetadata.builder()
                .channelTraceId(channelTraceId)
                .cifId(cifId)
                .build();

        FlexRuleEngineRequestDTO ruleEngineRequest = FlexRuleEngineRequestDTO.builder()
                .beneficiaryId(request.getBeneficiaryId())
                .customerAccountNo(request.getCustomerAccountNo())
                .accountCurrencyAmount(request.getAccountCurrencyAmount())
                .transactionCurrency(request.getTransactionCurrency())
                .accountCurrency(request.getAccountCurrency())
                .build();

        return Response.builder().data(flexRuleEngineService.getCharges(metadata, ruleEngineRequest)).build();

    }
}
