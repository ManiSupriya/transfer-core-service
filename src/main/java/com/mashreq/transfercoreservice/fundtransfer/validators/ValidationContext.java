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

    public void add(String key, Object value) {
        map.put(new ContextKey(key, value.getClass()), value);
    }

    public <T> T get(String key, Class<T> clazz) {
        ContextKey contextKey = new ContextKey(key, clazz);
        if (!map.containsKey(contextKey))
            return null;

        return clazz.cast(map.get(contextKey));
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
