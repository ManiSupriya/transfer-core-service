package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineResponseDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.FlexRuleEngineMWService;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

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


    private final FlexRuleEngineMWService flexRuleEngineMWService;

    @ApiOperation(value = "Fetch Rules for flex enginet", response = FlexRuleEngineResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    public Response transferFunds(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                                  @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                                  @RequestAttribute("X-CHANNEL-NAME") String channelName,
                                  @RequestHeader("X-CIF-ID") final String cifId,
                                  @Valid @RequestBody FlexRuleEngineRequestDTO request) {
        log.info("{} Flex Rule engine transfer for request received ", request);

        return Response.builder().data(FlexRuleEngineResponseDTO.builder()
                .charge(new BigDecimal(5))
                .productCode("FNSI")
        ).build();

    }
}
