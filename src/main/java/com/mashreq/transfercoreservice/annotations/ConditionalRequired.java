package com.mashreq.transfercoreservice.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shahbazkh
 * @date 3/4/20
 */

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(Conditionals.class)
@Constraint(validatedBy = ConditionalValidator.class)
public @interface ConditionalRequired {

    String fieldName();

    String dependentFieldName();

    String[] anyMatch() default {};

    String[] noneMatch() default {};

    boolean decode() default false;

    int size() default -1;

    String message() default "Must be greater than {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
