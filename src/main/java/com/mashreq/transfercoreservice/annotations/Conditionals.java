package com.mashreq.transfercoreservice.annotations;

import java.lang.annotation.*;

/**
 * @author shahbazkh
 * @date 3/5/20
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Conditionals {

    ConditionalRequired[] value();
}
