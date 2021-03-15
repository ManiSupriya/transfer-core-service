package com.mashreq.transfercoreservice.api;


import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.service.TransferEligibilityProxy;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferService;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferServiceDefault;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferServiceDefault fundTransferService;
    private final TransferEligibilityProxy transferEligibilityProxy;

    @ApiOperation(value = "Processes to start payment", response = FundTransferRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    public Response transferFunds(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
                                  @Valid @RequestBody FundTransferRequestDTO request) {

        log.info("{} Fund transfer for request received ", htmlEscape(request.getServiceType()));
        log.info("Fund transfer meta data created {} ", htmlEscape(metaData));
        if(request.getAmount() == null && request.getSrcAmount() ==null){
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_REQUEST, "Bad Request", "Both debitAmount and credit amount are missing");
        }
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(fundTransferService.transferFund(metaData, request)).build();
    }
    
    @ApiOperation(
    		value = "fund transfer eligibility to return supported payment types", 
    		response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    public Response<List<ServiceType>> retrieveEligibleServiceType(
    		@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
    		@Valid @RequestBody FundTransferRequestDTO request) {

        log.info("{} transfer eligibility request received for usertype ->{} and serviceType -> {}", htmlEscape(metaData.getUserType()), htmlEscape(request.getServiceType()));
        if(request.getAmount() == null && request.getSrcAmount() == null){
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_REQUEST, "Bad Request", "Both debitAmount and credit amount are missing");
        }
        return Response.<List<ServiceType>>builder()
                .status(ResponseStatus.SUCCESS)
                .data(transferEligibilityProxy.getEligibleServiceTypes(metaData, request)).build();
    }

}
