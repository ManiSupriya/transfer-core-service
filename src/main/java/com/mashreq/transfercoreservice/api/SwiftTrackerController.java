package com.mashreq.transfercoreservice.api;
/**
 * @author SURESH PASUPULETI
 */
import static org.springframework.web.util.HtmlUtils.htmlEscape;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.swifttracker.dto.GPITransactionsDetailsRes;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsReq;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsRes;
import com.mashreq.transfercoreservice.swifttracker.service.SwiftTrackerService;
import com.mashreq.webcore.dto.response.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/gpiTransaction")
@Api(value = "GPI Transactions")
@RequiredArgsConstructor
public class SwiftTrackerController {
	private final SwiftTrackerService swiftTrackerService;
	private final AsyncUserEventPublisher asyncUserEventPublisher;

	@ApiOperation(value = "Swift GPI Transaction Details for uetr", response = FundTransferRequestDTO.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 500, message = "Something went wrong") })
	@PostMapping("/swiftTransfer")
	public SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData, @Valid @RequestBody SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq) {
		log.info("Swift GPI Transaction Details for uetr {} ", htmlEscape(swiftGpiTransactionDetailsReq.getUetr()));
		return asyncUserEventPublisher.publishEvent(() ->swiftTrackerService.swiftGPITransactionDetails(metaData, swiftGpiTransactionDetailsReq), FundTransferEventType.SWIFT_GPI_TRANSACTION_DETAILS, metaData, FundTransferEventType.SWIFT_GPI_TRANSACTION_DETAILS.getDescription());
	}

	@ApiOperation("This operation is responsible for GPI Transaction Details.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "successfully processed"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 500, message = "Something went wrong") })
	@GetMapping
	public Response<List<GPITransactionsDetailsRes>> getSwiftMessageDetails(@RequestAttribute("X-REQUEST-METADATA") RequestMetaData metaData) {
		log.info("GPI Transaction Details for cif {} ", htmlEscape(metaData.getPrimaryCif()));
		return asyncUserEventPublisher.publishEvent(() ->swiftTrackerService.getSwiftMessageDetails(metaData), FundTransferEventType.GET_GPI_TRANSACTION_DETAILS, metaData, FundTransferEventType.GET_GPI_TRANSACTION_DETAILS.getDescription());

	}

}
