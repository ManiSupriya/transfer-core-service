/*
package com.mashreq.transfercoreservice.cache;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

@Component
@ConditionalOnBean({MobRedisConfig.class})
public class MobRedisService {
    private static final Logger log = LoggerFactory.getLogger(MobRedisService.class);
    @Autowired
    @Qualifier("mobRedisTemplate")
    private RedisTemplate<String, Object> mobRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${redis.write.ttl:60}")
    private long ttl;

    public MobRedisService() {
    }

    public <T> T get(final String key, final Class<T> clazz) {
        Object obj = this.mobRedisTemplate.opsForValue().get(key);
        T data = this.objectMapper.convertValue(obj, clazz);
        return data;
    }

    public <T> T get(final String key, final TypeReference<T> type) {
        Object obj = this.mobRedisTemplate.opsForValue().get(key);
        T data = this.objectMapper.convertValue(obj, type);
        return data;
    }

    public <T> void set(String key, T value) {
        log.info("Setting to cache key = {} value = {} ", key, value);
        this.mobRedisTemplate.opsForValue().set(key, value);
    }

    public <T> void setWithDefaultTTL(String key, T value) {
       // log.info("Setting to cache key = {} value = {} ", htmlEscape(key), htmlEscape(value));
        log.info("Setting to cache key with value");
        this.mobRedisTemplate.opsForValue().set(key, value);
        this.mobRedisTemplate.expire(key, this.ttl, TimeUnit.MINUTES);
    }

    public <T> void setWithTTL(String key, T value, long ttl) {
        log.info("Setting to cache key = {} value = {} ", key, value);
        this.mobRedisTemplate.opsForValue().set(key, value);
        this.mobRedisTemplate.expire(key, ttl, TimeUnit.MINUTES);
    }
}
*/
