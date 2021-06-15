package com.mashreq.transfercoreservice.paylater.utils;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.SIFrequencyType;
import com.mashreq.transfercoreservice.paylater.model.FundTransferOrder;

public class OrderExecutionDateResolverTest {

	@Test
	public void test_PayLater() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.PL);
		LocalDateTime nextExecutionTime = OrderExecutionDateResolver.getNextExecutionTime(order);
		assertNull(nextExecutionTime);
	}
	
	@Test
	public void test_SI_withSameStartDate() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.SI);
		order.setFrequency(SIFrequencyType.WEEK);
		order.setStartDate(LocalDateTime.now());
		order.setEndDate(LocalDateTime.now().plusDays(SIFrequencyType.WEEK.getExecutionIntervalIndays()-1));
		LocalDateTime nextExecutionTime = OrderExecutionDateResolver.getNextExecutionTime(order);
		assertNull(nextExecutionTime);
	}
	
	@Test
	public void test_SI_withDifferentStartDate() {
		FundTransferOrder order = new FundTransferOrder();
		order.setOrderType(FTOrderType.SI);
		order.setFrequency(SIFrequencyType.WEEK);
		order.setStartDate(LocalDateTime.now().plusDays(5));
		LocalDateTime nextExecutionTime = OrderExecutionDateResolver.getNextExecutionTime(order);
		assertEquals(order.getStartDate(), nextExecutionTime);
	}
}
