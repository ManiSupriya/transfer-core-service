package com.mashreq.transfercoreservice.swifttracker.service.impl;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
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

	@Override
	public SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(RequestMetaData metaData, SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq) {
		log.info("swiftGPITransactionDetails middle ware call started");
		return swiftTrackerMWService.swiftGPITransactionDetails(swiftGpiTransactionDetailsReq, metaData);
	}

	@Override
	public Response getSwiftMessageDetails(RequestMetaData metaData, String startDate, String endDate) {
		log.info("getSwiftMessageDetails middle ware call started");

		if (startDate == null || startDate.isEmpty()) {
			startDate = "1900-01-01";
		}
		if (endDate == null || endDate.isEmpty()) {
			endDate = (new Date()).toString();
		}
		return swiftMessageMWService.getSwiftMessageDetails(metaData, startDate, endDate);
	}

}
