package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class UAEAccountNumberResolverTest {
	private UAEAccountNumberResolver resolver;
	
	@Before
	public void init() {
		resolver = new UAEAccountNumberResolver(23, 12);
	}
	
	@Test
	public void test_ExceptionalIbanNumberScenario() {
		String accountNumber = resolver.generateAccountNumber(StringUtils.EMPTY);
		assertEquals(StringUtils.EMPTY, accountNumber);
	}

	@Test
	public void test_SuccessfulRetreive() {
		String accountNumber = resolver.generateAccountNumber("AE280330000010698008304");
		assertEquals("010698008304", accountNumber);
	}
}
