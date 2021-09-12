package com.mashreq.transfercoreservice.paylater.utils;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;

@RunWith(MockitoJUnitRunner.class)
public class SequenceNumberGeneratorTest {
	@Mock
	private FundTransferOrderRepository repository;
	
	private SequenceNumberGenerator generator;
	
	@Before
	public void init() {
		generator = new SequenceNumberGenerator(repository);
	}
	
	@Test
	public void test_success() {
		String sequence = "10000002";
		Mockito.when(repository.getNextFTOrderSequence()).thenReturn(Long.valueOf(sequence));
		String nextOrderId = generator.getNextOrderId();
		assertNotNull(nextOrderId);
		assertTrue(nextOrderId.contains(sequence));
		assertTrue(nextOrderId.contains(String.valueOf(LocalDate.now().getMonthValue())));
		assertEquals(12, nextOrderId.length());
	}

}
