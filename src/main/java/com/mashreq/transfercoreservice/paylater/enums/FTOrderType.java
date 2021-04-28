package com.mashreq.transfercoreservice.paylater.enums;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_FT_ORDER_TYPE;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.annotations.ValidEnum;

/**
 * @author indrajithg
 *
 */
public enum FTOrderType implements ValidEnum {
	SI("SI"),PL("PL"),PN("PN");
	private String name;
	@Override
	public String getName() {
		return this.name;
	}
	
	private FTOrderType(String name) {
		this.name = name;
	}
	
	private static final Map<String, FTOrderType> ftOrderTypeLookup = Stream.of(FTOrderType.values())
            .collect(Collectors.toMap(FTOrderType::getName, Function.identity()));

    public static FTOrderType getFTOrderTypeByName(String name) {
        if (!ftOrderTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_FT_ORDER_TYPE, INVALID_FT_ORDER_TYPE.getErrorMessage());

        return ftOrderTypeLookup.get(name);
    }
}
