package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class AddressLineSeparatorUtilTest {
	private int maxLength = 35;
	
	@Test
	public void test_with_benefullnamelengthlesserThanMaxlenthandOtherValuesAreNotEmpty() {
		/** test case with address line one having length more than allowed max length*/
		String[] separateAddressLineForSwift = AddressLineSeparatorUtil.separateAddressLineForSwift(maxLength, "Mashreq", "DBC near Al khail Metro Internet city", "Dubai", "United Arab Emirates");
		assertNotNull(separateAddressLineForSwift);
		assertEquals("DBC near Al khail Metro Internet ci", separateAddressLineForSwift[0]);
		assertEquals("ty Dubai", separateAddressLineForSwift[1]);
		assertEquals("United Arab Emirates", separateAddressLineForSwift[2]);
	}

	@Test
	public void test_with_benefullnamelengthlesserThanMaxLengthandaddressOneEmpty() {
		/** test case with address line two and three having length within in allowed length  and line one empty*/
		String fullName = "Mashreq";
		String city = "Dubai",country = "United Arab Emirates";
		String[] separateAddressLineForSwift = AddressLineSeparatorUtil.separateAddressLineForSwift(maxLength, fullName, StringUtils.EMPTY, city, country);
		assertNotNull(separateAddressLineForSwift);
		assertNull(separateAddressLineForSwift[0]);
		assertEquals(city,separateAddressLineForSwift[1]);
		assertEquals(country,separateAddressLineForSwift[2]);
	}
	
	@Test
	public void test_with_benefullnamelengthyandOtherValuesAreNull() {
		/** test case with address line two and three having length within in allowed length  and line one empty*/
		String fullName = "Mashreq bank Dubai United Arab Emirates";
		String[] separateAddressLineForSwift = AddressLineSeparatorUtil.separateAddressLineForSwift(maxLength, fullName, StringUtils.EMPTY, null, null);
		assertNotNull(separateAddressLineForSwift);
		assertEquals("ates", separateAddressLineForSwift[0]);
		assertNull(separateAddressLineForSwift[2]);
		assertNull(separateAddressLineForSwift[1]);
	}
	
	@Test
	public void test_with_benefullnamelengthandline1lengthy() {
		String fullName = "Mashreq bank Dubai United Arab Emirates";
		String line1 = "Mashreq bank Dubai United Arab Emirates";
		String line3 = "United Arab Emirates";
		String[] separateAddressLineForSwift = AddressLineSeparatorUtil.separateAddressLineForSwift(maxLength, fullName, line1, StringUtils.EMPTY, line3);
		assertNotNull(separateAddressLineForSwift);
		assertEquals("ates Mashreq bank Dubai United Arab", separateAddressLineForSwift[0]);
		assertEquals(line3,separateAddressLineForSwift[2]);
		assertEquals(" Emirates", separateAddressLineForSwift[1]);
	}
	
	@Test
	public void test_with_benefullname_line1andline2lengthy() {
		String fullName = "Mashreq bank Dubai United Arab Emirates";
		String line1 = "Mashreq bank Dubai United Arab Emirates";
		String line2 = "Digital Studio DBC third floor";
		String line3 = "United Arab Emirates";
		String[] separateAddressLineForSwift = AddressLineSeparatorUtil.separateAddressLineForSwift(maxLength, fullName, line1, line2, line3);
		assertNotNull(separateAddressLineForSwift);
		assertEquals("ates Mashreq bank Dubai United Arab", separateAddressLineForSwift[0]);
		assertEquals("floor United Arab Emirates",separateAddressLineForSwift[2]);
		assertEquals(" Emirates Digital Studio DBC third ", separateAddressLineForSwift[1]);
	}
	
	//TODO: write a test case with full name null
}
