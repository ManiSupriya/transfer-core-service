package com.mashreq.transfercoreservice.paylater.enums;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SI_FREQUENCY_TYPE;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.annotations.ValidEnum;

/**
 * Frequency Type for Standing instructions
 *
 * @author indrajithg
 */
public enum SIFrequencyType implements ValidEnum {

    DAY("DAY"), WEEK("WEEK"), MONTH("MONTH");
	private String name;
	
	private SIFrequencyType (String typeName) {
		this.name = typeName;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	private static final Map<String, SIFrequencyType> siFrequencyTypeLookup = Stream.of(SIFrequencyType.values())
            .collect(Collectors.toMap(SIFrequencyType::getName, Function.identity()));

    public static SIFrequencyType getSIFrequencyTypeByName(String name) {
        if (!siFrequencyTypeLookup.containsKey(name))
            GenericExceptionHandler.handleError(INVALID_SI_FREQUENCY_TYPE, INVALID_SI_FREQUENCY_TYPE.getErrorMessage());

        return siFrequencyTypeLookup.get(name);
    }
}