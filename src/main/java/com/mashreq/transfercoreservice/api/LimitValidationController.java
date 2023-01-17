package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.dto.LimitValidatorRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@RestController
@RequestMapping("/v1/limit")
@Api(value = "Limit Validation")
@RequiredArgsConstructor
public class LimitValidationController {
    private final LimitValidator limitValidator;
    @ApiOperation(value = "Validate Limits", response = FundTransferRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Validated Limits"),
            @ApiResponse(code = 401, message = "Unauthorized  error")
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
