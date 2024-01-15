package com.mashreq.transfercoreservice.paylater.utils;


import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashreq.transfercoreservice.paylater.repository.FundTransferOrderRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SequenceNumberGeneratorTest {
	@Mock
	private FundTransferOrderRepository repository;
	
	private SequenceNumberGenerator generator;
	
	@BeforeEach
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
