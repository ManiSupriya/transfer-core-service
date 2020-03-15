package com.mashreq.transfercoreservice.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.*;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shahbazkh
 */
@NotNull(message = "Amount cannot be empty")
@Positive(message = "Should be positive")
@Digits(fraction = 2, integer = 6, message = "Amount should be in 6 digits with two fraction")
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface TransactionAmount {
    String message() default "Amount in not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
