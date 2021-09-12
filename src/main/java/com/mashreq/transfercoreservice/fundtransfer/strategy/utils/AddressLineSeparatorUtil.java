package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author indrajithg
 *
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressLineSeparatorUtil {

	public static String[] separateAddressLineForSwift(int maxLength, String fullName, String address1, String address2, String address3) {
		String[] result = new String[3];
		/** extracting right part of beneficiary full name */
		String addr1 = prepareFirstLine(maxLength, fullName, address1, result);
		log.debug("First line is :: {}", addr1);
		String addr2 = prepareSecondLine(maxLength, address2, result, addr1);
		log.debug("Second line is :: {}", addr2);
		String addr3 = prepareThirdLine(maxLength, address3, result, addr2);
    	log.debug("third line is :: {}", addr3);
    	return result;
	}

	protected static String prepareThirdLine(int maxLength, String address3, String[] result, String addr2) {
		String addr3;
		if(StringUtils.isNotBlank(addr2)&& addr2.length() > maxLength) {
			addr3 = addr2.substring(maxLength)+(StringUtils.isNotBlank(address3) ? " "+address3 : StringUtils.EMPTY);
		}else {
			addr3 = address3;
		}
		if(StringUtils.isNotBlank(addr3)) {
			result[2] = StringUtils.left(addr3, maxLength);
		}else {
			result[2] = null;
		}
		return addr3;
	}

	protected static String prepareSecondLine(int maxLength, String address2, String[] result, String addr1) {
		String addr2;
		if(StringUtils.isNotBlank(addr1)&& addr1.length() > maxLength) {
			addr2 = addr1.substring(maxLength)+(StringUtils.isNotBlank(address2) ? " "+address2 : StringUtils.EMPTY);
		}else {
			addr2 = address2;
		}
		
		if(StringUtils.isNotBlank(addr2)) {
			result[1] = StringUtils.left(addr2, maxLength);
		}else {
			result[1] = null;
		}
		return addr2;
	}

	protected static String prepareFirstLine(int maxLength, String fullName, String address1, String[] result) {
		String addr1;
		if(StringUtils.isNotBlank(fullName)&& fullName.length() > maxLength) {
			addr1 = fullName.substring(maxLength)+ (StringUtils.isNotBlank(address1) ? " "+address1 : StringUtils.EMPTY);
		}else {
			addr1 = address1;
		}
		if(StringUtils.isNotBlank(addr1)) {
			result[0] = StringUtils.left(addr1, maxLength);
		}else {
			result[0] = null;
		}
		return addr1;
	}
}
