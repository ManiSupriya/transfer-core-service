package com.mashreq.transfercoreservice.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.stream.Stream;

import static org.apache.commons.beanutils.BeanUtils.getProperty;

/**
 * @author shahbazkh
 * @date 3/4/20
 */
@Slf4j
public class ConditionalValidator implements ConstraintValidator<ConditionalRequired, Object> {

    public static final String EMPTY_STRING = "";
    private String fieldName;
    private String dependentFieldName;
    private String[] anyMatch;
    private String[] nonMatch;
    private int size;
    private boolean decodeBase64;

    @Override
    public void initialize(ConditionalRequired constraintAnnotation) {
        fieldName = constraintAnnotation.fieldName();
        dependentFieldName = constraintAnnotation.dependentFieldName();
        anyMatch = constraintAnnotation.anyMatch();
        nonMatch = constraintAnnotation.noneMatch();
        size = constraintAnnotation.size();
        decodeBase64 = constraintAnnotation.decode();

    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        try {
            final String dependentFieldValue = getDependentFieldValue(dependentFieldName, object);
            if (null != anyMatch && anyMatch.length > 0) {
                if (Stream.of(anyMatch).anyMatch(x -> x.equals(dependentFieldValue))) {
                    final String fieldValue = getFieldValue(fieldName, decodeBase64, object);
                    if (size != -1 && fieldValue.length() != size) {
                        return false;
                    } else {
                        return fieldValue.length() > 0;
                    }
                }
            }

            if (null != nonMatch && nonMatch.length > 0) {
                if (Stream.of(nonMatch).noneMatch(x -> x.equals(dependentFieldValue))) {
                    final String fieldValue = getFieldValue(fieldName, decodeBase64, object);
                    if (size != -1 && fieldValue.length() != size) {
                        return false;
                    } else {
                        return fieldValue.length() > 0;
                    }
                }
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error occurred while resolving custom annotations ", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    private String getFieldValue(final String fieldName, final boolean decodeBase64, final Object object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final String fieldValue = StringUtils.trim(getProperty(object, fieldName));
        return StringUtils.isBlank(fieldValue)
                ? EMPTY_STRING
                : getDecodedValue(decodeBase64, fieldValue);

    }

    private String getDecodedValue(boolean decodeBase64, String fieldValue) {
        return decodeBase64
        ? new String(Base64.getDecoder().decode(fieldValue))
        : fieldValue;
    }

    private String getDependentFieldValue(final String dependentFieldName, final Object object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return StringUtils.trim(getProperty(object, dependentFieldName));
    }
}
