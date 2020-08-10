package com.mashreq.transfercoreservice.swifttracker.service.impl;
/**
 * @author SURESH PASUPULETI
 */
import org.springframework.stereotype.Service;

import com.mashreq.esbcore.bindings.account.mbcdm.SWIFTGPITransactionDetailsReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.SWIFTGPITransactionDetailsResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.swiftgpitransactiondetails.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsReq;
import com.mashreq.transfercoreservice.swifttracker.dto.SWIFTGPITransactionDetailsRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftTrackerMWService {
	
	private final WebServiceClient webServiceClient;
	private final HeaderFactory headerFactory;
	private final SoapServiceProperties soapServiceProperties;

	public SWIFTGPITransactionDetailsRes swiftGPITransactionDetails(SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq, RequestMetaData metaData){
		log.info("Swift GPI Transaction Details call initiated [ {} ]", swiftGpiTransactionDetailsReq);

        EAIServices response = (EAIServices) webServiceClient.exchange(generateSwiftTrackerEaiServices(swiftGpiTransactionDetailsReq, metaData));
        SWIFTGPITransactionDetailsResType responseDTO = response.getBody().getSWIFTGPITransactionDetailsRes();
		return SWIFTGPITransactionDetailsRes.builder().initiationTime(responseDTO.getInitiationTime())
				.lastUpdateTime(responseDTO.getLastUpdateTime()).paymentEventDetails(responseDTO.getPaymentEventDetails())
				.transactionStatus(responseDTO.getTransactionStatus()).uetr(responseDTO.getUETR())
				.build();
		
	}
	 private EAIServices generateSwiftTrackerEaiServices(SWIFTGPITransactionDetailsReq swiftGpiTransactionDetailsReq, RequestMetaData metaData) {
		 EAIServices request = new EAIServices();
	     request.setBody(new EAIServices.Body());
	     HeaderType headerType =headerFactory.getHeader(soapServiceProperties.getServiceCodes().getSwiftGpiTransactionDetails(), metaData.getChannelTraceId());     
	        headerType.setMsgVersion("V1.1.1.1");
	        headerType.setSrcAppReqId("20170818114213");
	        // headerType.setSrcAppTimestamp("CurrentStamp");
	        headerType.setSrcAppSessionId("SessionId");
	        headerType.setSrvName("GPITransactionDetails");
	        headerType.setSrvOpCode("GPITransactionDetails");
	        headerType.setTargetApp("CBS");
	        //headerType.setEAITimestamp(new XMLGregorianCalendar());
	        headerType.setTrackingId("fd6aeadc-7e6d-11e7-b86c-0a0406850000");
	        headerType.setStatus("S");
	     request.setHeader(headerType);
	     SWIFTGPITransactionDetailsReqType swiftGPITransactionDetailsReqType = new SWIFTGPITransactionDetailsReqType();
	     swiftGPITransactionDetailsReqType.setUETR(swiftGpiTransactionDetailsReq.getUetr());
	     request.getBody().setSWIFTGPITransactionDetailsReq(swiftGPITransactionDetailsReqType);
	     return request;
	 }
}
