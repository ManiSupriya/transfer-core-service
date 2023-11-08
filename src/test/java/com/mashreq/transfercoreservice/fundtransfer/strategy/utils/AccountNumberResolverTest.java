package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class AccountNumberResolverTest {
	private AccountNumberResolver resolver;

	@Test
	public void test_ExceptionalIbanNumberScenario() {
		resolver = new AccountNumberResolver(23, 12);
		String accountNumber = resolver.generateAccountNumber(StringUtils.EMPTY);
		assertEquals(StringUtils.EMPTY, accountNumber);
	}

	@Test
	public void test_SuccessfulRetreive() {
		resolver = new AccountNumberResolver(23, 12);
		String accountNumber = resolver.generateAccountNumber("AE280330000010698008304");
		assertEquals("010698008304", accountNumber);
	}

	@Test
	public void test_EgyptSuccessfulRetreive() {
		resolver = new AccountNumberResolver(29, 12);
		String accountNumber = resolver.generateAccountNumber("EG690046000400000059040009945");
		assertEquals("059040009945", accountNumber);
	}
}
