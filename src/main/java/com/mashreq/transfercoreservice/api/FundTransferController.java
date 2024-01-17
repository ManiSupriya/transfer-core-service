package com.mashreq.transfercoreservice.api;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.service.TransferLimitService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.transactionauth.annotations.RequiresAuthorization;
import org.apache.commons.lang.StringUtils;

import com.mashreq.dedupe.annotation.UniqueRequest;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.dto.HandleNotificationRequestDto;
import com.mashreq.transfercoreservice.dto.NotificationRequestDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.*;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.duplicateRequestValidation.FundsTransferRequestResolver;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.service.TransferEligibilityProxy;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferFactory;
import com.mashreq.transfercoreservice.fundtransfer.service.NpssEnrolmentService;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/transfer")
@Tag(name= "FundTransferController",description = "Fund Transfer")
@RequiredArgsConstructor
public class FundTransferController {
    private final FundTransferFactory serviceFactory;
    private final TransferEligibilityProxy transferEligibilityProxy;
    private final NpssEnrolmentService npssEnrolmentService;
    private final TransferLimitService transferLimitService;

    @Operation(summary = "Processes to start payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
    })
    @PostMapping
    @RequiresAuthorization( failOnMissingOtp = false)
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
    
    @Operation(
    		summary = "fund transfer eligibility to return supported payment types"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
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


    @Operation(
    		summary = "API to records the transfer limit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
    })
    @PostMapping("/saveTransferDetails/{transactionRefNo}")
    public Response<TransferLimitResponseDto> saveTransferDetails(
    		@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData,
    		@Valid @RequestBody TransferLimitRequestDto request, @PathVariable final String transactionRefNo) {

        log.info("Received transfer details to save {}", htmlEscape(request));
        return Response.<TransferLimitResponseDto>builder()
                .status(ResponseStatus.SUCCESS)
                .data(transferLimitService.validateAndSaveTransferDetails(request, transactionRefNo))
        .build();
    }


    @Operation(
            summary = "check npss enrolment of the user")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfully processed"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "500", description = "Something went wrong") })
    @GetMapping("/enrolmentStatus")
    public Response retrieveNpssEnrolment(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData) {
        log.info("check npss enrolment of the user {} ", htmlEscape(metaData.getUserType()));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(npssEnrolmentService.checkEnrolment(metaData)).build();
    }
    @Operation(
            summary = "update npss enrolment of the user")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successfully processed"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "500", description = "Something went wrong") })
    @GetMapping("/updateEnrolmentStatus")
    public Response updateNpssEnrolment(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData) {
        log.info("check npss enrolment of the user {} ", htmlEscape(metaData.getUserType()));
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(npssEnrolmentService.updateEnrolment(metaData)).build();
    }

    @Operation(summary = "Handle Successful  Transaction of NPSS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Handled Successful Transaction for NPSS"),
            @ApiResponse(responseCode = "401", description = "Unauthorized error")
    })
    @PostMapping("/npss/handle-transaction")
    public Response handleTransaction(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData,
                                      @RequestBody HandleNotificationRequestDto handleNotificationRequestDto) {
        log.info("Handle Transaction Initiated for the cif {} {}", requestMetaData.getPrimaryCif(),handleNotificationRequestDto.toString());
        npssEnrolmentService.handleTransaction(requestMetaData,handleNotificationRequestDto);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Transaction Handled Successfully.")
                .build();
    }

    @Operation(summary = "Handle Successful  Transaction of NPSS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Handled Successful Transaction for NPSS"),
            @ApiResponse(responseCode = "401", description = "Unauthorized error")
    })
    @PostMapping("/npss/notifications")
    public Response handleNotifications(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData, @Valid @RequestBody NotificationRequestDto notificationRequestDto) {
        log.info("notifications call Initiated for the cif {} {}", requestMetaData.getPrimaryCif(),notificationRequestDto.toString());
        npssEnrolmentService.performNotificationActivities(requestMetaData,notificationRequestDto);
        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Notifications Sent Successfully.")
                .build();
    }

}
