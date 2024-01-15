package com.mashreq.transfercoreservice.api;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.commons.cache.HeaderNames;
import com.mashreq.transfercoreservice.dto.TransactionHistoryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.transactionqueue.TransactionHistoryService;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Slf4j
@RestController
@RequestMapping("/v1/payment-history")
@Tag(name = "TransactionHistoryController",description = "Transaction History Controller")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;

    @Operation(summary = "Processes to start payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
    })
    @GetMapping("/charity-paid/{serviceType}")
    public Response transferFunds(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData,
                                  @RequestHeader(HeaderNames.CIF_HEADER_NAME) final String cifId,
                                  @NotNull @PathVariable("serviceType") final String serviceType) {

        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(transactionHistoryService.getCharityPaid(cifId, serviceType))
                .build();
    }

    @Operation(summary = "Save Transaction History")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully saved Transaction"),
            @ApiResponse(responseCode = "401", description = "Unauthorized  error")
    })
    @PostMapping("/save/transaction")
    public Response saveTransactionHistory(@RequestAttribute(Constants.X_REQUEST_METADATA) RequestMetaData requestMetaData, TransactionHistoryDto transactionHistoryDto) {

        log.info("Inserting into Transaction History table {} ", htmlEscape(requestMetaData.getPrimaryCif()));

        return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(transactionHistoryService.saveTransactionHistory(transactionHistoryDto, requestMetaData))
                .message("Transaction Saved Successfully in Transaction History.")
                .build();
    }

    @Operation(summary = "Get Transaction History from data base")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
    })
    @GetMapping(value = "/transactionDetail")
    public Response<TransactionHistoryDto> getTransactionDetail(@RequestParam String paymentId) {
        log.info("Getting Transaction History Details for the paymentId : {} ", paymentId);
        return Response.<TransactionHistoryDto>builder()
                .status(ResponseStatus.SUCCESS)
                .data(transactionHistoryService.getTransactionDetailByHostRef(paymentId))
                .build();
    }

    @Operation(summary = "Get Transaction History from data base")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed"),
            @ApiResponse(responseCode = "500", description = "Something went wrong")
    })
    @GetMapping(value = "/transactionHistory")
    public Response<List<TransactionHistoryDto>> getTransactionHistory(@RequestParam String cif,@RequestParam String startDate,@RequestParam String endDate) {
        log.info("Getting Transaction History Details for the paymentId : {} ", cif);
        return Response.<List<TransactionHistoryDto>>builder()
                .status(ResponseStatus.SUCCESS)
                .data(transactionHistoryService.getTransactionHistoryByCif(cif,startDate,endDate))
                .build();
    }
}
