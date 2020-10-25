package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.esbcore.bindings.account.mbcdm.AccountSummaryReqType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.accountdetails.EAIServices;

@RunWith(MockitoJUnitRunner.class)
public class AccountDetailsEaiServicesTest {
	AccountDetailsEaiServices accountDetailsEaiServices = new AccountDetailsEaiServices();
	@Mock
	EAIServices eaiServices;
	
	@Test
	public void accountDetailsEaiServicesTest() {
		 AccountSummaryReqType reqType = new AccountSummaryReqType();
	        reqType.setAccountNo("test");
	        AccountDetailsEaiServices.Body body = new AccountDetailsEaiServices.Body();
	        body.setAccountDetailsReq(reqType);
	        eaiServices.setBody(body);
	    accountDetailsEaiServices = new AccountDetailsEaiServices(eaiServices);
		 
	    assertEquals(eaiServices.getBody(), accountDetailsEaiServices.getBody());
	}
	@Test
	public void accountDetailsEaiServicesFailTest() {
		try {
	    accountDetailsEaiServices = new AccountDetailsEaiServices(null);
		}catch(RuntimeException re) {
			assertTrue(re.getMessage().contains("Passing wrong object type"));
		}
	}
}
