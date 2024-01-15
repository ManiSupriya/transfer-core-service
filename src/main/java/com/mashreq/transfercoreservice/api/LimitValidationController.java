package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.LimitValidatorRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@RestController
@RequestMapping("/v1/limit")
@Tag(name = "LimitValidationController",description = "Limit Validation Controller")
@RequiredArgsConstructor
public class LimitValidationController {
    private final LimitValidator limitValidator;
    @Operation(summary = "Validate Limits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Validated Limits"),
            @ApiResponse(responseCode = "401", description = "Unauthorized  error")
    })
    @PostMapping("/validation")
    public Response validateLimit(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData,@RequestBody LimitValidatorRequestDto limitValidatorRequestDto) {

        log.info("Limit Validation  {} ", htmlEscape(requestMetaData.getPrimaryCif()));
         Long beneId = null!=limitValidatorRequestDto.getBeneId() ? limitValidatorRequestDto.getBeneId() : 0l;
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(limitValidator.validate(limitValidatorRequestDto.getUserDTO(),limitValidatorRequestDto.getBeneficiaryType(),limitValidatorRequestDto.getPaidAmount() ,requestMetaData,beneId))
                .message("Transaction Limit Check Validated Successfully.")
                .build();
    }
}
