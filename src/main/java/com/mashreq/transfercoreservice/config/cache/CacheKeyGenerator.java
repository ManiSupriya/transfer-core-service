package com.mashreq.transfercoreservice.config.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author shahbazkh
 * @date 4/2/20
 */
public class CacheKeyGenerator implements KeyGenerator {

    public Object generate(Object target, Method method, Object... params) {
        return (target.getClass().getSimpleName() + "-"
                + method.getName() + "-"
                + StringUtils.arrayToDelimitedString(params, "-")).toLowerCase();
    }
}
