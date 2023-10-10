package com.mashreq.transfercoreservice.fundtransfer.validators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;

@ExtendWith(MockitoExtension.class)
public class CCTransactionEligibilityValidatorTest {
	private CCTransactionEligibilityValidator validator;
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	private final String CARD_NO = "123456789";
	
	@BeforeEach
	public void init() {
		validator = new CCTransactionEligibilityValidator(auditEventPublisher);
	}
	
	@Test
	public void test_NonCCTransaction_shouldReturnValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setToAccount(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertTrue(result.isSuccess());
	}

	@Test
	public void test_LocalCCTransaction_shouldReturnValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setServiceType(ServiceType.LOCAL.getName());
		request.setCardNo(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertTrue(result.isSuccess());
	}
	
	@Test
	public void test_INFTCCTransaction_shouldReturnInValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setServiceType(ServiceType.INFT.getName());
		request.setCardNo(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertFalse(result.isSuccess());
		assertEquals(TransferErrorCode.CC_TRX_NOT_ALLOWED, result.getTransferErrorCode());
	}
	
	@Test
	public void test_WYMACCTransaction_shouldReturnInValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setServiceType(ServiceType.WAMA.getName());
		request.setCardNo(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertFalse(result.isSuccess());
		assertEquals(TransferErrorCode.CC_TRX_NOT_ALLOWED, result.getTransferErrorCode());
	}
	
	@Test
	public void test_WAMACCTransaction_shouldReturnInValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setServiceType(ServiceType.WYMA.getName());
		request.setCardNo(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertFalse(result.isSuccess());
		assertEquals(TransferErrorCode.CC_TRX_NOT_ALLOWED, result.getTransferErrorCode());
	}
	
	@Test
	public void test_PL_CCTransaction_shouldReturnInValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setServiceType(ServiceType.WYMA.getName());
		request.setOrderType("PL");
		request.setCardNo(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertFalse(result.isSuccess());
		assertEquals(TransferErrorCode.CC_TRX_NOT_ALLOWED, result.getTransferErrorCode());
	}
	
	@Test
	public void test_SI_CCTransaction_shouldReturnInValid() {
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		RequestMetaData metadata = new RequestMetaData();
		request.setServiceType(ServiceType.WYMA.getName());
		request.setOrderType("SI");
		request.setCardNo(CARD_NO);
		ValidationResult result = validator.validate(request , metadata);
		assertFalse(result.isSuccess());
		assertEquals(TransferErrorCode.CC_TRX_NOT_ALLOWED, result.getTransferErrorCode());
	}
}
