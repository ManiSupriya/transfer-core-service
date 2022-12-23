package com.mashreq.transfercoreservice.fundtransfer.duplicateRequestValidation;

import com.mashreq.dedupe.dto.DedupeRequestDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
	public void test_resolver_checkifSkipDedupeLogicIsWOrking() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		String finTxnNo = "finTxnNo-fundstransfer";
		request.setFinTxnNo(finTxnNo );
		request.setServiceType("WYMA");
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
		assertEquals(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST.customErrorCode(), resolveUniqueRequest.getDuplicateRequestErrorCode());
		assertEquals(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST.getErrorMessage(), resolveUniqueRequest.getDuplicateRequestErrorDesc());
	}

	@Test
	public void should_skip_duplicate_check_when_otp_is_present() {
		// Given
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		String finTxnNo = "finTxnNo-fundstransfer";
		request.setFinTxnNo(finTxnNo);
		request.setServiceType("WYMA");
		request.setOtp("otp");

		// When
		DedupeRequestDto resolveUniqueRequest = resolver.resolveUniqueRequest(request);

		// Then
		assertNotNull(resolveUniqueRequest);
		assertTrue(resolveUniqueRequest.isSkipDedupe());
		assertEquals(finTxnNo, resolveUniqueRequest.getUniqueIdentifiers());
		assertEquals(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST.customErrorCode(), resolveUniqueRequest.getDuplicateRequestErrorCode());
		assertEquals(TransferErrorCode.DUPLICATION_FUND_TRANSFER_REQUEST.getErrorMessage(), resolveUniqueRequest.getDuplicateRequestErrorDesc());
	}
}
