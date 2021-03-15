package com.mashreq.transfercoreservice.fundtransfer.dto;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_USER_TYPE;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mashreq.ms.exceptions.GenericExceptionHandler;

public enum CustomerClientType {

	RETAIL("RETAIL"),
	SME("SME");

	private String value;

	CustomerClientType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	private static final Map<String, CustomerClientType> lookup = Stream.of(CustomerClientType.values())
            .collect(Collectors.toMap(CustomerClientType::name, customerClientType -> customerClientType));

    public static CustomerClientType getCustomerClientType(String name) {
        if (!lookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_USER_TYPE, INVALID_USER_TYPE.getErrorMessage());

        return lookup.get(name);
    }

}
