package com.mashreq.transfercoreservice.config.feign;

import org.springframework.context.annotation.Bean;

import feign.codec.ErrorDecoder;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

public class FeignConfig {

    @Bean
    public FeignAccessTokenInterceptor interceptRequest() {
        return new FeignAccessTokenInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignCustomErrorDecoder();
    }


}
