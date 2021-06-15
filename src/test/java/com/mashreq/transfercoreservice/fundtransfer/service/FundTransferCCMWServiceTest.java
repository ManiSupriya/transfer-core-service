package com.mashreq.transfercoreservice.fundtransfer.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferCCResType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.fundtransfercc.EAIServices;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.config.FTCCConfig;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapClient;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;

@RunWith(MockitoJUnitRunner.class)
public class FundTransferCCMWServiceTest {
	
	@InjectMocks
	FundTransferCCMWService fundTransferCCMWService;
	@Mock
	HeaderFactory headerFactory;
	@Mock
	SoapServiceProperties soapServiceProperties;
	@Mock
	FTCCConfig ftCCConfig;
	@Mock
	FundTransferRequest fundTransferRequest;
	@Mock
	RequestMetaData requestMetaData;
	 @Mock
	 AsyncUserEventPublisher asyncUserEventPublisher;
	 @Mock
	 SoapClient mobSoapClient;
	 @Mock
	 EAIServices eaiServices;
	
	@Before
	 public void prepare() {
	     fundTransferRequest = generateFundTransferRequest();
	     requestMetaData = generateRequestMetaData();
	 }
	
	@Test
    public void getTransferTestSucess() throws IOException {
		eaiServices = new EAIServices();
		FundTransferCCResType fundTransferCCResType = new FundTransferCCResType();
		fundTransferCCResType.setCardReferenceNumber("1234");
		fundTransferCCResType.setCardStatus("active");
		fundTransferCCResType.setCoreReferenceNumber("123");
		fundTransferCCResType.setCoreStatus("test");
		HeaderType header = new HeaderType();
		EAIServices.Body body = new EAIServices.Body();
		body.setFundTransferCCRes(fundTransferCCResType);
		eaiServices.setBody(body);
		eaiServices.setHeader(header);
		/*Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.when(mobSoapClient.exchange(Mockito.any())).thenReturn(eaiServices);
		fundTransferCCMWService.transfer(fundTransferRequest, requestMetaData);*/
	}
	
	@Test
    public void getTransferTestFailure() {
		Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		try {
		fundTransferCCMWService.transfer(fundTransferRequest, requestMetaData);
		}catch(Throwable throwable) {
			GenericException genericException = (GenericException) throwable;
			Assert.assertEquals(genericException.getErrorCode(), "TN-8006");
		}
	}
	
	@Test
	public void testTransferWithNonAED() {
		fundTransferRequest.setDestinationCurrency("INR");
		Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		try {
			fundTransferCCMWService.transfer(fundTransferRequest, requestMetaData);
		} catch (Throwable throwable) {
			GenericException genericException = (GenericException) throwable;
			Assert.assertEquals(genericException.getErrorCode(), "TN-8006");
		}
	}
	
	@Test
	public void testTransferWithAED() {
		fundTransferRequest.setDestinationCurrency("AED");
		Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		try {
			fundTransferCCMWService.transfer(fundTransferRequest, requestMetaData);
		} catch (Throwable throwable) {
			GenericException genericException = (GenericException) throwable;
			Assert.assertEquals(genericException.getErrorCode(), "TN-8006");
		}
	}
	
	private FundTransferRequest generateFundTransferRequest() {
		return FundTransferRequest.builder().acwthInst1("")
		 .acwthInst2("test1")
		 .acwthInst5("test1")
		 .amount(new BigDecimal("100"))
		 .awInstBICCode("test")
		 .awInstName("test1")
		 .beneficiaryAddressOne("test1")
		 .beneficiaryAddressThree("test1")
		 .beneficiaryAddressTwo("test1")
		 .beneficiaryFullName("test1")
		 .cardNo("test1")
		 .channel("test1")
		 .channelTraceId("test1")
		 .chargeBearer("test1")
		 .dealNumber("test1")
		 .dealRate(new BigDecimal("2.75"))
		 .destinationCurrency("AED")
		 .destinationISOCurrency("AE")
		 .exchangeRate(new BigDecimal("100"))
		 .expiryDate("10-10-2020")
		 .finTxnNo("132")
		 .fromAccount("123456")
		 .internalAccFlag("true")
		 .limitTransactionRefNo("123456")
		 .NotificationType("email")
		 .productId("1234")
		 .purposeCode("CLC")
		 .purposeDesc("car loan")
		 .sourceBranchCode("021")
		 .sourceCurrency("AED")
		 .sourceISOCurrency("PKR")
		 .sourceOfFund("AED")
		 .srcAmount(new BigDecimal("100"))
		 .status("ACTIVE")
		 .transactionCode("123")
		 .transferType("INFT")
		 .txnCurrency("AED")
		 .build();
		 }
	private RequestMetaData generateRequestMetaData() {
		requestMetaData = new RequestMetaData();
		requestMetaData.setChannel("MOB");
    	requestMetaData.setChannelTraceId("testChannelTraceID");
    	requestMetaData.setLoginId("12345");
    	return requestMetaData;
    }
}