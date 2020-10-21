package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.esbcore.bindings.customer.mbcdm.FlexRuleEngineResType;
import com.mashreq.esbcore.bindings.customer.mbcdm.GatewayDetailsType;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices.Body;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMWRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.FlexRuleEngineMWResponse;
import com.mashreq.transfercoreservice.middleware.HeaderFactory;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties.ServiceCodes;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;

@RunWith(MockitoJUnitRunner.class)
public class FlexRuleEngineMWServiceTest {
	
	@InjectMocks
	FlexRuleEngineMWService flexRuleEngineMWService;
	@Mock
	WebServiceClient webServiceClient;
	@Mock
	FlexRuleEngineResponseHandler responseHandler;
	@Mock
	RequestMetaData requestMetaData;
	@Mock
	FlexRuleEngineMWRequest request;
	@Mock
	EAIServices eaiServices;
	@Mock
	HeaderFactory headerFactory;
	@Mock
	SoapServiceProperties soapServiceProperties;
	@Mock
	ServiceCodes serviceCodes;
	@Mock
	AsyncUserEventPublisher asyncUserEventPublisher;
	@Mock
	FlexRuleEngineResType responseDTO;
	@Mock
	GatewayDetailsType gatewayDetailsType;
	@Mock
	List<GatewayDetailsType> gatewayDetailsTypeList;
	
	@Test
	public void getRulesTest() {
		 FlexRuleEngineResType reqType = new FlexRuleEngineResType();
		 	reqType.setProductCode("AED");
		 	reqType.setCustAccountNo("1234");
		 	reqType.setTransactionCurrency("AED");
		 	reqType.setTransactionAmount("123");
		 	reqType.setAccountCurrency("AED");
		 	reqType.setAccountCurrencyAmount("123");
		 	reqType.setDealNumber("AED");
		 	reqType.setTransactionStatus("AED");
		 	reqType.setValueDate("AED");
		 	reqType.setAccountWithInstitution("AED");
	        reqType.setTransferType("AED");
	        gatewayDetailsType = new GatewayDetailsType();
	        gatewayDetailsType.setProductCode("AED");
	        gatewayDetailsType.setTransactionCurrency("AED");
	        gatewayDetailsType.setTransactionAmount("123");
	        gatewayDetailsType.setAccountCurrency("AED");
	        gatewayDetailsType.setAccountCurrencyAmount("123");
	        gatewayDetailsType.setExchangeRate("123");
	        reqType.getGatewayDetails().add(gatewayDetailsType);
	        Body body = new Body();
	        body.setFlexRuleEngineRes(reqType);
	        eaiServices.setBody(body);
	        request.setChannelTraceId("12");
	        serviceCodes.setFlexRuleEngine("test");
	        request.setChannelTraceId("test");
	         soapServiceProperties.setServiceCodes(serviceCodes);

		Mockito.when(webServiceClient.exchange(Mockito.any())).thenReturn(eaiServices);
		Mockito.when(request.getChannelTraceId()).thenReturn("test");
		Mockito.when(eaiServices.getBody()).thenReturn(body);
		Mockito.when(soapServiceProperties.getServiceCodes()).thenReturn(serviceCodes);
		Mockito.doNothing().when(responseHandler).validateResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doNothing().when(asyncUserEventPublisher).publishSuccessfulEsbEvent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		FlexRuleEngineMWResponse flexRuleEngineMWResponse = flexRuleEngineMWService.getRules(request, requestMetaData);
		assertEquals("AED", flexRuleEngineMWResponse.getProductCode());
	}
	
}
