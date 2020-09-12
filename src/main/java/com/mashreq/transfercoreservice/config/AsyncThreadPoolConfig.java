package com.mashreq.transfercoreservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async task executor
 * 
 * @author PallaviG
 *
 */

@Slf4j
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {
	
	@Value("${async.thread.poolName}")
	private String poolName;
	@Value("${async.thread.corePoolSize}")
	private Integer corePoolSize;
	@Value("${async.thread.maxPoolSize}")
	private Integer maxPoolSize;
	@Value("${async.thread.queueCapacity}")
	private Integer queueCapacity;
	@Value("${async.thread.keepAliveSeconds}")
	private Integer keepAliveSeconds;
	

	 @Bean ("GenericAsyncExecutor")
	    public Executor asyncExecutor() {
	        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	        executor.setCorePoolSize(corePoolSize);
	        executor.setMaxPoolSize(maxPoolSize);
		 	executor.setWaitForTasksToCompleteOnShutdown(true);
	        executor.setThreadNamePrefix(poolName);
	        executor.setQueueCapacity(queueCapacity);
	        executor.setKeepAliveSeconds(keepAliveSeconds);
	        executor.initialize();
	        
	        log.info("GenericAsyncExecutorConfigPool is set!");
	        return executor;
	    }

}
