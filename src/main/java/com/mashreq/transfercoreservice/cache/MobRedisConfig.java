/*
package com.mashreq.transfercoreservice.cache;

*/
/**
 * Suresh Pasupuleti
 *//*

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@EnableCaching
@Configuration
@ConditionalOnProperty(
        name = {"redis.enable"},
        havingValue = "true",
        matchIfMissing = false
)
public class MobRedisConfig {
    private static final Logger log = LoggerFactory.getLogger(MobRedisConfig.class);
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private Integer redisPort;
    @Value("${redis.password}")
    private String redisPass;
    @Value("${redis.ssl}")
    private boolean useSsl;

    @Bean
    @Primary
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory(this.buildPoolConfig());
        factory.setHostName(this.redisHost);
        factory.setPort(this.redisPort);
        factory.setUsePool(true);
        factory.setUseSsl(useSsl);
        if (this.redisPass != null) {
            factory.setPassword(this.redisPass);
        }

        log.info("Redis Connection Pool is set ");
        return factory;
    }

    //@Primary
    @Bean({"mobRedisTemplate"})
    RedisTemplate<String, Object> mobRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(this.jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer(Object.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    private JedisPoolConfig buildPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60L).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30L).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}
*/
