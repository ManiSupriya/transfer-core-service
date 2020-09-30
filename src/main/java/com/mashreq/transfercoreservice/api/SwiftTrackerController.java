package com.mashreq.transfercoreservice.api;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.GPI_TRACKER_URL;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.GPI_TRANSACTIONS;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.REQ_METADATA;
import static com.mashreq.transfercoreservice.swifttracker.commonconstants.SwiftTransferConstants.SWIFT_TRACKER;
/**
 * @author SURESH PASUPULETI
 */
import static org.springframework.web.util.HtmlUtils.htmlEscape;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping(GPI_TRACKER_URL)
@Api(value = GPI_TRANSACTIONS)
@RequiredArgsConstructor
@Validated
public class SwiftTrackerController {
	private final SwiftTrackerService swiftTrackerService;
	private final AsyncUserEventPublisher asyncUserEventPublisher;

	@ApiOperation(value = "Swift GPI Transaction Details for uetr", response = FundTransferRequestDTO.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 500, message = "Something went wrong") })
	@PostMapping(SWIFT_TRACKER)
	public SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(@RequestAttribute(REQ_METADATA) RequestMetaData metaData, @Valid @RequestBody SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq) {
		log.info("Swift GPI Transaction Details for uetr {} ", htmlEscape(swiftGpiTransactionDetailsReq.getUetr()));
		return asyncUserEventPublisher.publishEvent(() ->swiftTrackerService.swiftGPITransactionDetails(metaData, swiftGpiTransactionDetailsReq), FundTransferEventType.SWIFT_GPI_TRANSACTION_DETAILS, metaData, FundTransferEventType.SWIFT_GPI_TRANSACTION_DETAILS.getDescription());
	}

	@ApiOperation("This operation is responsible for GPI Transaction Details.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "successfully processed"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 500, message = "Something went wrong") })
	@GetMapping
	public Response<List<GPITransactionsDetailsRes>> getSwiftMessageDetails(
			@RequestAttribute(REQ_METADATA) RequestMetaData metaData,
			@RequestParam(required = true) @NotNull @Pattern(regexp = "^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$", message = "Please use yyyy-mm-dd ") String startDate,
			@RequestParam(required = true) @NotNull @Pattern(regexp = "^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$", message = "Please use yyyy-mm-dd ") String endDate) {
		log.info("GPI Transaction Details for cif {} ", htmlEscape(metaData.getPrimaryCif()));
		return asyncUserEventPublisher.publishEvent(() ->swiftTrackerService.getSwiftMessageDetails(metaData, startDate, endDate), FundTransferEventType.GET_GPI_TRANSACTION_DETAILS, metaData, FundTransferEventType.GET_GPI_TRANSACTION_DETAILS.getDescription());

	}

}
