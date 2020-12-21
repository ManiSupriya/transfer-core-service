package com.mashreq.transfercoreservice.fundtransfer.validators;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shahbazkh
 * @date 3/17/20
 */

@Slf4j
public class ValidationContext {

    private Map<String, Object> map;

    public ValidationContext() {
        map = new HashMap<>();
    }

    public void add(String key, Object value) {
        map.put(key, value);
    }

    public <T> T get(String key, Class<T> clazz) {
        if (!map.containsKey(key)) {
            log.warn("Cannot find any data with key [ {} ] in Validation Context ", key);
        }
        return clazz.cast(map.get(key));
    }
}
