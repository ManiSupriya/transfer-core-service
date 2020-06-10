package com.mashreq.transfercoreservice.config.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author shahbazkh
 * @date 3/9/20
 */
@Component
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ChannelTraceInterceptor channelTraceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(channelTraceInterceptor);
    }
}
