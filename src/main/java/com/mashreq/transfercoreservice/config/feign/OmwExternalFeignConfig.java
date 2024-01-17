package com.mashreq.transfercoreservice.config.feign;

import com.mashreq.mobcommons.cache.MobRedisService;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Created by KrishnaKo on 05/01/2024
 */
public class OmwExternalFeignConfig {
    @Autowired
    private OmwExternalConfigProperties overlayConfigProperties;
    @Autowired
    private Tracer tracer;
    @Autowired
    RestTemplate restTemplate;

    @Bean
    public OmwExternalHeaderInterceptor interceptRequest() {
        return new OmwExternalHeaderInterceptor(overlayConfigProperties, tracer, restTemplate);
    }


}
