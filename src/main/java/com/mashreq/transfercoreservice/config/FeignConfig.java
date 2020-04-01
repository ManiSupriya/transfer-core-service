package com.mashreq.transfercoreservice.config;

import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Configuration
public class FeignConfig {

    @Bean
    public FeignAccessTokenInterceptor interceptRequest() {
        return new FeignAccessTokenInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignCustomErrorDecoder();
    }

    @Bean
    public Decoder decoder() {
        return new FeignDecoder();
    }

}
