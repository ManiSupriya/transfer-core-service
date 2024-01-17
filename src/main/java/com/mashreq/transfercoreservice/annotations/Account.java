package com.mashreq.transfercoreservice.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shahbazkh
 */
@NotBlank(message = "Account Number cannot be empty")
@Size(min = 7, max = 34, message = "Account number should be 7 to 34 digits")
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Account {
    String message() default "Valid Account Number is required ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
