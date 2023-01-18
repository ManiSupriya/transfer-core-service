package com.mashreq.transfercoreservice.api;


import com.mashreq.dedupe.annotation.UniqueRequest;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.dto.HandleNotificationRequestDto;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.duplicateRequestValidation.FundsTransferRequestResolver;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.service.TransferEligibilityProxy;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferFactory;
import com.mashreq.transfercoreservice.fundtransfer.service.NpssEnrolmentService;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
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
import java.util.List;
import java.util.Map;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Api(value = "Fund Transfer")
@RequiredArgsConstructor
public class FundTransferController {
    private final FundTransferFactory serviceFactory;
    private final TransferEligibilityProxy transferEligibilityProxy;
    private final NpssEnrolmentService npssEnrolmentService;

    @ApiOperation(value = "Processes to start payment", response = FundTransferRequestDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping
    @UniqueRequest(clazz = FundsTransferRequestResolver.class,flowName = Constants.FUND_TRANSFER_REQUEST,enableIdentifierHashing = false)
    public Response transferFunds(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
                                  @Valid @RequestBody FundTransferRequestDTO request) {
        log.info("{} Fund transfer for request received ", htmlEscape(request.getServiceType()));
        log.info("Fund transfer meta data created {} ", htmlEscape(metaData));
        if(request.getAmount() == null && request.getSrcAmount() ==null){
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_REQUEST, "Bad Request", "Both debitAmount and credit amount are missing");
        }
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(serviceFactory.getServiceAppropriateService(request).transferFund(metaData, request)).build();
    }
    
    @ApiOperation(
    		value = "fund transfer eligibility to return supported payment types", 
    		response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully processed"),
            @ApiResponse(code = 500, message = "Something went wrong")
    })
    @PostMapping("/paymentType")
    public Response<Map<ServiceType,EligibilityResponse>> retrieveEligibleServiceType(
    		@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
    		@Valid @RequestBody FundTransferEligibiltyRequestDTO request) {

        log.info("{} transfer eligibility request received for usertype ->{} and serviceType -> {}", htmlEscape(metaData.getUserType()), htmlEscape(request.getServiceType()));
        if(request.getAmount() == null){
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_REQUEST, "Bad Request", "Both debitAmount and credit amount are missing");
        }
        return Response.<Map<ServiceType,EligibilityResponse>>builder()
                .status(ResponseStatus.SUCCESS)
                .data(transferEligibilityProxy.checkEligibility(metaData, request)).build();
    }

    @ApiOperation(
            value = "check npss enrolment of the user",
            response = NpssEnrolmentStatusResponseDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "successfully processed"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 500, message = "Something went wrong") })
    @GetMapping("/enrolmentStatus")
    public Response retrieveNpssEnrolment(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData) {
        log.info("check npss enrolment of the user {} ", htmlEscape(metaData.getUserType()));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(npssEnrolmentService.checkEnrolment(metaData)).build();
    }
    @ApiOperation(
            value = "update npss enrolment of the user",
            response = NpssEnrolmentUpdateResponseDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "successfully processed"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 500, message = "Something went wrong") })
    @GetMapping("/updateEnrolmentStatus")
    public Response updateNpssEnrolment(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData) {
        log.info("check npss enrolment of the user {} ", htmlEscape(metaData.getUserType()));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(npssEnrolmentService.updateEnrolment(metaData)).build();
    }

    @ApiOperation(value = "Handle Successful  Transaction of NPSS", response = NotificationRequestDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Handled Successful Transaction for NPSS"),
            @ApiResponse(code = 401, message = "Unauthorized error")
    })
    @PostMapping("/npss/handle-transaction")
    public Response handleTransaction(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData, HandleNotificationRequestDto handleNotificationRequestDto) {

        npssEnrolmentService.handleTransaction(requestMetaData,handleNotificationRequestDto);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Transaction Handled Successfully.")
                .build();
    }

    @ApiOperation(value = "Handle Successful  Transaction of NPSS")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Handled Successful Transaction for NPSS"),
            @ApiResponse(code = 401, message = "Unauthorized error")
    })
    @PostMapping("/npss/notifications")
    public Response handleNotifications(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData, @Valid @RequestBody NotificationRequestDto notificationRequestDto) {
        npssEnrolmentService.performNotificationActivities(requestMetaData,notificationRequestDto);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Notifications Sent Successfully.")
                .build();
    }
}
