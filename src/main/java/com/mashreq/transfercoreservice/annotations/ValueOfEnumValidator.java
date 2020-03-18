package com.mashreq.transfercoreservice.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {

    private List<String> acceptedValues;
    private boolean isRequired;

    @Override
    public void initialize(ValueOfEnum annotation) {
        isRequired = annotation.isRequired();
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(validEmum -> (ValidEnum) validEmum)
                .map(ValidEnum::getName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {

        //mandatory but value not present
        if (isRequired && value == null) {
            return false;
        }

        // value not present and not mandatory
        if (value == null) {
            return true;
        }

        return acceptedValues.contains(value.toString());
    }
}