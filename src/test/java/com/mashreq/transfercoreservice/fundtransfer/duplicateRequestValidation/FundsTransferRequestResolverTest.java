package com.mashreq.transfercoreservice.fundtransfer.duplicateRequestValidation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mashreq.dedupe.dto.DedupeRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

public class FundsTransferRequestResolverTest {

	private FundsTransferRequestResolver resolver;
	@Before
	public void init() {
		resolver = new FundsTransferRequestResolver();
	}
	
	@Test(expected = NullPointerException.class)
	public void test_resolver_with_null() {
		resolver.resolveUniqueRequest(null);
	}

	@Test(expected = NullPointerException.class)
	public void test_resolver_with_nullFinTxnNo() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		resolver.resolveUniqueRequest(request );
	}
	
	@Test
	public void test_resolver_checkifREsponseContainsFinTxnNo() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		String finTxnNo = "finTxnNo-fundstransfer";
		request.setFinTxnNo(finTxnNo );
		DedupeRequestDto resolveUniqueRequest = resolver.resolveUniqueRequest(request);
		assertNotNull(resolveUniqueRequest);
		/** this value should be false always, 
		 * so that duplicate request validation logic will gets executed
		 * as per request-dedupe-util version 1.0.5
		*/
		assertFalse(resolveUniqueRequest.isSkipDedupe());
		/**
		 * repeating the same logic for identifying unique request from FE */
		assertEquals(finTxnNo, resolveUniqueRequest.getUniqueIdentifiers());
	}
}
