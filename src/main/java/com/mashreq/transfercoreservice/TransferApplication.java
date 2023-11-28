package com.mashreq.transfercoreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashreq.mobcommons.autoconfig.FeignInterceptorConfig;
import com.mashreq.notification.client.annotations.EnableTemplateNotificationClient;
import com.mashreq.tracing.annotation.EnableMashreqTracing;
import com.mashreq.transactionauth.annotations.EnableTransactionAuthorization;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.text.NumberFormat;

@Slf4j
@EnableFeignClients
@EnableTransactionAuthorization
@EnableMashreqTracing
@EnableScheduling
@EnableTemplateNotificationClient
@SpringBootApplication(scanBasePackages = {"com.mashreq.transfercoreservice", "com.mashreq.ms","com.mashreq.mobcommons"})
public class TransferApplication {

    @Autowired
    private ObjectMapper objectMapper;

    public static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
        printMetrix();

    }

    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Print Memory information
     */
    private static void printMetrix() {
        Runtime runtime = Runtime.getRuntime();
        final NumberFormat format = NumberFormat.getInstance();
        final long maxMemory = runtime.maxMemory();
        final long allocatedMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final long mb = 1024L * 1024L;
        final String mega = " MB";

        log.info(" ========================== Memory Info ========================== ");
        log.info("Free memory: " + format.format(freeMemory / mb) + mega);
        log.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega);
        log.info("Max memory: " + format.format(maxMemory / mb) + mega);
        log.info("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / mb) + mega);
        log.info("=================================================================\n");
    }
}
