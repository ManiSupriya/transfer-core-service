package com.mashreq.transfercoreservice.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shahbazkh
 */
@NotBlank(message = "Account Number cannot be empty")
@Size(min = 9, max = 34, message = "Account number should be 9 to 34 digits")
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Account {
    String message() default "Valid Account Number is required ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
