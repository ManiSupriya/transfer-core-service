package com.mashreq.transfercoreservice.event.conf;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;


import java.util.concurrent.Executor;

@Slf4j
@Configuration
public class AsyncAuditEventConfig {

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(@Qualifier("taskExecutor") Executor taskExecutor) {
        SimpleApplicationEventMulticaster eventMultiCaster = new SimpleApplicationEventMulticaster();
        eventMultiCaster.setTaskExecutor(taskExecutor);
        eventMultiCaster.setErrorHandler(throwable -> log.error("[AsyncAuditEventConfig] Error while publishing events to audit table", throwable));
        return eventMultiCaster;
    }
}
