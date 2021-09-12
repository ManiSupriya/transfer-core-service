package com.mashreq.transfercoreservice.fundtransfer.validators;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

@RunWith(MockitoJUnitRunner.class)
public class AccountFreezeValidatorTest {
	private Validator<FundTransferRequestDTO> validator;
	@Mock
	private AsyncUserEventPublisher auditEventPublisher;
	
	@Before
	public void init() {
		validator = new AccountFreezeValidator(auditEventPublisher);
	}
	
	@Test
	public void test() {
		RequestMetaData metadata = new RequestMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		ValidationContext context = new ValidationContext();
		context.add("validate-debit-freeze", Boolean.TRUE);
		SearchAccountDto accDto = new SearchAccountDto();
		accDto.setNoCredit(true);
		accDto.setNoDebitForCompliance(true);
        context.add("debit-account-details", accDto);
		ValidationResult validate = validator.validate(request, metadata, context);
		assertFalse(validate.isSuccess());
	}


	@Test
	public void test_DebitFreeze() {
		RequestMetaData metadata = new RequestMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		ValidationContext context = new ValidationContext();
		context.add("validate-debit-freeze", Boolean.TRUE);
		SearchAccountDto accDto = new SearchAccountDto();
		accDto.setNoCredit(true);
		accDto.setNoDebit(true);
        context.add("debit-account-details", accDto);
		ValidationResult validate = validator.validate(request, metadata, context);
		assertFalse(validate.isSuccess());
	}
	
	@Test
	public void test_CreditFreeze() {
		RequestMetaData metadata = new RequestMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		ValidationContext context = new ValidationContext();
		context.add("validate-credit-freeze", Boolean.TRUE);
		SearchAccountDto accDto = new SearchAccountDto();
		accDto.setNoCredit(true);
		context.add("credit-account-details", accDto);
		ValidationResult validate = validator.validate(request, metadata, context);
		assertFalse(validate.isSuccess());
	}

	@Test
	public void test_NoFreeze() {
		RequestMetaData metadata = new RequestMetaData();
		FundTransferRequestDTO request = new FundTransferRequestDTO();
		ValidationContext context = new ValidationContext();
		context.add("validate-credit-freeze", Boolean.TRUE);
		context.add("validate-debit-freeze", Boolean.TRUE);
		SearchAccountDto accDto = new SearchAccountDto();
		accDto.setNoCredit(false);
		accDto.setNoDebit(false);
		accDto.setNoDebitForCompliance(false);
		context.add("credit-account-details", accDto);
		context.add("debit-account-details", accDto);
		ValidationResult validate = validator.validate(request, metadata, context);
		assertTrue(validate.isSuccess());
	}
}
