package com.mashreq.transfercoreservice.fundtransfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.esbcore.bindings.account.mbcdm.AccountSummaryReqType;
import com.mashreq.esbcore.bindings.accountservices.mbcdm.accountdetails.EAIServices;

@ExtendWith(MockitoExtension.class)
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
