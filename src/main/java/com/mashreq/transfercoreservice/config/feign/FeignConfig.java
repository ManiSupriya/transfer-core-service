package com.mashreq.transfercoreservice.config.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

//    @Bean
//    public Decoder decoder() {
//        return new FeignDecoder();
//    }


}
