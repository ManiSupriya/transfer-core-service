package com.mashreq.transfercoreservice.paylater.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateTimeUtilTest {

	@Test
	public void test_convertToDateTime() {
		DateTimeUtil instance = DateTimeUtil.getInstance();
		LocalDateTime convertToDateTime = instance.convertToDateTime("2020-03-22 09:15:00",instance.DATE_TIME_FORMATTER_LONG);
		convertToDateTime.plusHours(6);
		assertNotNull(convertToDateTime);
	}

	@Test
	public void test_convertToZonedDateTime() {
		DateTimeUtil instance = DateTimeUtil.getInstance();
		LocalDateTime convertToDateTime = instance.getCurrentDateTimeZone();
		System.out.println(convertToDateTime);
		assertNotNull(convertToDateTime);
	}
	
}
