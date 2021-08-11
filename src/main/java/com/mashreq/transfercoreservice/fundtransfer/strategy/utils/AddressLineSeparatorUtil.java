package com.mashreq.transfercoreservice.fundtransfer.strategy.utils;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author indrajithg
 *
 */
@Slf4j
public class AddressLineSeparatorUtil {

	public static String[] separateAddressLineForSwift(int maxLength, String fullName, String address1, String address2, String address3) {
		String[] result = new String[3];
		/** extracting right part of beneficiary full name */
		String addr1=null,addr2=null,addr3=null;
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
		log.debug("First line is :: {}", addr1);
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
		log.debug("Second line is :: {}", addr2);
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
    	log.debug("third line is :: {}", addr3);
    	return result;
	}
}
