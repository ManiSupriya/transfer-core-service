package com.mashreq.transfercoreservice.fundtransfer.validators;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author shahbazkh
 * @date 3/17/20
 */
public class ValidationContext {

    private Map<ContextKey, Object> map;

    public ValidationContext() {
        map = new HashMap<>();
    }

    public void add(String key, Class clazz, Object value) {
        map.put(new ContextKey(key, clazz), value);
    }

    public <T> T get(String key, Class<T> clazz) {
        if (!map.containsKey(new ContextKey(key, clazz)))
            throw new RuntimeException("Error while fetching Validation context");

        return clazz.cast(map.get(key));
    }

    private static class ContextKey {
        String key;
        Class clazz;

        ContextKey(String key, Class clazz) {
            this.key = key;
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextKey that = (ContextKey) o;
            return Objects.equals(key, that.key) &&
                    Objects.equals(clazz, that.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, clazz);
        }
    }
}
