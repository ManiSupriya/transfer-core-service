package com.mashreq.transfercoreservice.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shahbazkh
 */
@NotBlank(message = "Account Number cannot be empty")
@Pattern(regexp = "[\\s]*[0-9]+", message = "Account Number should be Numeric")
@Size(min = 9, max = 12, message = "Account number should be 9 to 12 digits")
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Account {
    String message() default "9 to 12 digit Account Number is required ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
