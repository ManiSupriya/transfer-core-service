package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices;
import com.mashreq.esbcore.bindings.customerservices.mbcdm.flexruleengine.EAIServices.Body;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;

@RunWith(MockitoJUnitRunner.class)
public class FlexRuleEngineResponseHandlerTest {
	@InjectMocks
	FlexRuleEngineResponseHandler flexRuleEngineResponseHandler;
	@Mock
	EAIServices response;
	@Mock
	RequestMetaData metaData;
	@Mock
	AsyncUserEventPublisher asyncUserEventPublisher;
	String remarks;
	String srcMsgId;
	@Mock
	GenericExceptionHandler genericExceptionHandler;
	
	@Before
	 public void prepare() {
	     this.flexRuleEngineResponseHandler.init();
	 }
	
	@Test
	public void validateResponseTest() {
		HeaderType headerType = new HeaderType();
		headerType.setStatus("Failure");
		ErrorType errorType = new ErrorType();
		errorType.setErrorCode("ACC-ESB-001");
		errorType.setErrorDescription("Failure");
		Body body = new Body();
		body.setExceptionDetails(errorType);
		response.setHeader(headerType);
		response.setBody(body);
		Mockito.when(response.getBody()).thenReturn(body);
		Mockito.when(response.getHeader()).thenReturn(headerType);
		Mockito.doNothing().when(asyncUserEventPublisher).publishFailedEsbEvent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		try {
			flexRuleEngineResponseHandler.validateResponse(response, metaData, remarks, srcMsgId);
			}catch(Throwable t) {
				assertTrue(t.getMessage().contains("Failure"));
			}
	}
}
