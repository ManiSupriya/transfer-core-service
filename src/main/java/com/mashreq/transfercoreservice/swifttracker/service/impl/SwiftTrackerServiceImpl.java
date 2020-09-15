package com.mashreq.transfercoreservice.swifttracker.service.impl;
import static com.mashreq.transfercoreservice.common.CommonConstants.SWIFT_GPI_TRANSACTION_DETAILS;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.NOT_VALID_DATE_GPI_TRACKER;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.NOT_VALID_END_DATE_GPI_TRACKER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsReq;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsRes;
import com.mashreq.transfercoreservice.swifttracker.service.SwiftTrackerService;
import com.mashreq.webcore.dto.response.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftTrackerServiceImpl implements SwiftTrackerService{
	private final SwiftTrackerMWService swiftTrackerMWService;
	private final SwiftMessageMWService swiftMessageMWService;
	private final AsyncUserEventPublisher asyncUserEventPublisher;
	private static final int MONTH_VAL = 6;

	@Override
	public SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(RequestMetaData metaData, SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq) {
		log.info("swiftGPITransactionDetails middle ware call started");
		return swiftTrackerMWService.swiftGPITransactionDetails(swiftGpiTransactionDetailsReq, metaData);
	}

	@Override
	public Response getSwiftMessageDetails(RequestMetaData metaData, String startDate, String endDate) {
		log.info("getSwiftMessageDetails middle ware call started");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		int months = (int) ChronoUnit.MONTHS.between(LocalDate.parse(startDate, formatter), LocalDateTime.now()); 
		log.info("time difference to validate start date in months {}",months);
		if(months>MONTH_VAL) {
			log.info("failed to fetch the details as start date is more than 6 months old");
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.SWIFT_GPI_TRANSACTION_DETAILS,
					metaData, SWIFT_GPI_TRANSACTION_DETAILS, metaData.getChannelTraceId(),
					NOT_VALID_DATE_GPI_TRACKER.toString(),
					NOT_VALID_DATE_GPI_TRACKER.getErrorMessage(),
					NOT_VALID_DATE_GPI_TRACKER.getErrorMessage());
			GenericExceptionHandler.handleError(NOT_VALID_DATE_GPI_TRACKER, NOT_VALID_DATE_GPI_TRACKER.getErrorMessage(), NOT_VALID_DATE_GPI_TRACKER.getErrorMessage());	
		}
		if(LocalDate.parse(endDate, formatter).isBefore(LocalDate.parse(startDate, formatter))) {
			log.info("failed to fetch the details as end date is before start date");
			asyncUserEventPublisher.publishFailedEsbEvent(FundTransferEventType.SWIFT_GPI_TRANSACTION_DETAILS,
					metaData, SWIFT_GPI_TRANSACTION_DETAILS, metaData.getChannelTraceId(),
					NOT_VALID_END_DATE_GPI_TRACKER.toString(),
					NOT_VALID_END_DATE_GPI_TRACKER.getErrorMessage(),
					NOT_VALID_END_DATE_GPI_TRACKER.getErrorMessage());
			GenericExceptionHandler.handleError(NOT_VALID_END_DATE_GPI_TRACKER, NOT_VALID_END_DATE_GPI_TRACKER.getErrorMessage(), NOT_VALID_END_DATE_GPI_TRACKER.getErrorMessage());	
		}
		return swiftMessageMWService.getSwiftMessageDetails(metaData, startDate, endDate);
	}

}
