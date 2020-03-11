package com.mashreq.transfercoreservice.api;

import com.mashreq.transfercoreservice.paymentoptions.PaymentOptionRequest;
import com.mashreq.transfercoreservice.paymentoptions.PaymentOptionType;
import com.mashreq.transfercoreservice.paymentoptions.PaymentOptionsService;
import com.mashreq.webcore.dto.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.mashreq.transfercoreservice.paymentoptions.PaymentOptionType.getPaymentOptionsByType;

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class FundTransferController {

    @ApiOperation(value = "Processes to start payment", response = FundTransferRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    public Response transferFunds(@RequestHeader("X-CIF-ID") final String cifId,
                                  @Valid @RequestBody FundTransferRequestDTO request) {
        return Response.builder().data(request).build();
    }
}
