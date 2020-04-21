package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
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


    @ApiOperation(value = "Fetch Rules for flex enginet", response = FlexRuleEngineDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping("/{countryCode}")
    public Response transferFunds(@RequestAttribute("X-CHANNEL-TRACE-ID") String channelTraceId,
                                  @RequestAttribute("X-CHANNEL-HOST") String channelHost,
                                  @RequestAttribute("X-CHANNEL-NAME") String channelName,
                                  @RequestHeader("X-CIF-ID") final String cifId,
                                  @Valid @RequestBody FlexRuleEngineRequestDTO request) {
        log.info("{} Flex Rule engine transfer for request received ", request);
        FlexRuleEngineDTO response = FlexRuleEngineDTO.builder().productCode("FNSI").charge(new BigDecimal(5)).build();
        return Response.builder().data(response).build();

    }
}
