package com.mashreq.transfercoreservice.api;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageService;
import com.mashreq.transfercoreservice.fundtransfer.user.DigitalUserService;
import com.mashreq.transfercoreservice.model.DigitalUser;
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
@RequestMapping("/v1/digital-limit")
@Tag(name = "Digital Limit Usage")
@RequiredArgsConstructor
public class DigitalLimitUsageController {

    private final DigitalUserService digitalUserService;
    private final DigitalUserLimitUsageService digitalUserLimitUsageService;
    @Operation(summary = "Save Digital Limit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Saved Digital User Limit"),
            @ApiResponse(responseCode = "401", description = "Unauthorized error")
    })
    @PostMapping("/save")
    public Response saveDigitalLimitUsage(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData,@RequestBody DigitalUserLimitUsageDTO digitalUserLimitUsageDTO) {

        DigitalUser digitalUser = digitalUserService.getDigitalUser(requestMetaData);
        digitalUserLimitUsageDTO.setDigitalUserId(digitalUser.getId());
        digitalUserLimitUsageDTO.setCreatedBy(String.valueOf(digitalUser.getId()));
        log.info("Save Digital Limit Usage  for Cif{} ", htmlEscape(requestMetaData.getPrimaryCif()));
        digitalUserLimitUsageService.insert(digitalUserLimitUsageDTO);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Digital Limit Saved Successfully.")
                .build();
    }
}
