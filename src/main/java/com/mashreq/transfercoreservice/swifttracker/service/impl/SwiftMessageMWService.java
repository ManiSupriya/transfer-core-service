package com.mashreq.transfercoreservice.swifttracker.service.impl;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mashreq.esbcore.bindings.account.mbcdm.GPITransactionsDetailsReqType;
import com.mashreq.esbcore.bindings.account.mbcdm.GPITransactionsDetailsType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.gpitransactionsdetails.EAIServices;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.swifttracker.dto.GPITransactionsDetailsRes;
import com.mashreq.webcore.dto.response.Response;
import com.mashreq.webcore.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftMessageMWService {
	
	private final WebServiceClient webServiceClient;
	private final HeaderFactory headerFactory;
	private final SoapServiceProperties soapServiceProperties;

	public Response<Object> getSwiftMessageDetails(RequestMetaData metaData, String startDate, String endDate){
		log.info("GPI Transaction Details call initiated [ {} ]", htmlEscape(metaData.getPrimaryCif()));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        EAIServices response = (EAIServices) webServiceClient.exchange(generateSwiftMessageEaiServices(metaData));
		return Response.builder()
                .status(ResponseStatus.SUCCESS)
                .data(response.getBody().getGPITransactionsDetailsRes().getGPITransactionsDetails()
                        .stream()
                        .filter(date -> (LocalDate.parse(date.getDate(), formatter)
                        		.isAfter(LocalDate.parse(startDate, formatter)) || LocalDate.parse(date.getDate(), formatter)
                        		.equals(LocalDate.parse(startDate, formatter))) && (LocalDate.parse(date.getDate(), formatter)
                        		.isBefore(LocalDate.parse(endDate, formatter)) || LocalDate.parse(date.getDate(), formatter)
                        		.equals(LocalDate.parse(endDate, formatter))))
                        .map(this::convert)
                        .collect(Collectors.toList()))
                .build();
		
	}

	 private EAIServices generateSwiftMessageEaiServices(RequestMetaData metaData) {
		 EAIServices request = new EAIServices();
	     request.setBody(new EAIServices.Body());
	     HeaderType headerType = headerFactory.getHeader(soapServiceProperties.getServiceCodes().getGpiTransactionDetails(), metaData.getChannelTraceId());
	     request.setHeader(headerType);
	     GPITransactionsDetailsReqType gpiTransactionsDetailsReqType = new GPITransactionsDetailsReqType();
	     gpiTransactionsDetailsReqType.setCIFId(metaData.getPrimaryCif());
	     request.getBody().setGPITransactionsDetailsReq(gpiTransactionsDetailsReqType);;
	     return request;
	 }
	 
	 private GPITransactionsDetailsRes convert(GPITransactionsDetailsType gpiTransactionsDetailsResType) {
	        
	        return GPITransactionsDetailsRes.builder()
	                .cifId(gpiTransactionsDetailsResType.getCIFId())
	                .creditAccount(gpiTransactionsDetailsResType.getCreditAccount())
	                .creditAccountBranch(gpiTransactionsDetailsResType.getCreditAccountBranch())
	                .creditAccountCcy(gpiTransactionsDetailsResType.getCreditAccountCcy())
	                .creditAmount(gpiTransactionsDetailsResType.getCreditAccount())
	                .transactionRefNo(gpiTransactionsDetailsResType.getTransactionRefNo())
	            	.uetr(gpiTransactionsDetailsResType.getUETR())
	            	.ultBeneficiary1(gpiTransactionsDetailsResType.getUltBeneficiary1())
	            	.ultBeneficiary2(gpiTransactionsDetailsResType.getUltBeneficiary2())
	            	.debitAccount(gpiTransactionsDetailsResType.getDebitAccount())
	            	.debitAccountBranch(gpiTransactionsDetailsResType.getDebitAccountBranch())
	            	.debitAccountCcy(gpiTransactionsDetailsResType.getDebitAccountCcy())
	            	.debitAmount(gpiTransactionsDetailsResType.getDebitAmount())
	            	.date(gpiTransactionsDetailsResType.getDate())
	                .build();
	    }
}
