package com.mashreq.transfercoreservice.paylater.enums;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.annotations.ValidEnum;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;

/**
 * Frequency Type for Standing instructions
 *
 * @author indrajithg
 */
public enum SIFrequencyType implements ValidEnum {

    DAY("DAY",1), WEEK("WEEK",7), MONTH("MONTH",30);
	private String name;
	private int executionIntervalIndays;
	
	private SIFrequencyType (String typeName,int executionIntervalIndays) {
		this.name = typeName;
		this.executionIntervalIndays = executionIntervalIndays;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	public int getExecutionIntervalIndays() {
		return this.executionIntervalIndays;
	}
	private static final Map<String, SIFrequencyType> siFrequencyTypeLookup = Stream.of(SIFrequencyType.values())
            .collect(Collectors.toMap(SIFrequencyType::getName, Function.identity()));

    public static SIFrequencyType getSIFrequencyTypeByName(String name) {
        if (!siFrequencyTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(TransferErrorCode.INVALID_SI_FREQUENCY_TYPE, TransferErrorCode.INVALID_SI_FREQUENCY_TYPE.getErrorMessage());

        return siFrequencyTypeLookup.get(name);
    }
}