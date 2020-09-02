package com.mashreq.transfercoreservice.swifttracker.service;
/**
 * @author SURESH PASUPULETI
 */
import java.util.List;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.swifttracker.dto.GPITransactionsDetailsRes;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsReq;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsRes;
import com.mashreq.webcore.dto.response.Response;

public interface SwiftTrackerService {
	SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(RequestMetaData metaData, SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq);
	Response<List<GPITransactionsDetailsRes>> getSwiftMessageDetails(RequestMetaData metaData);

}
