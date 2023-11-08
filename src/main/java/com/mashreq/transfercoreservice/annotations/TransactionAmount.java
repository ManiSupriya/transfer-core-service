package com.mashreq.transfercoreservice.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shahbazkh
 */
@Positive(message = "Should be positive")
@Digits(fraction = 2, integer = 15, message = "Amount should be in 15 digits with two fraction")
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface TransactionAmount {
    String message() default "Amount in not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
