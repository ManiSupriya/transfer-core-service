package com.mashreq.transfercoreservice.scheduler;

import com.mashreq.transfercoreservice.fundtransfer.service.NpssEnrolmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAccountUpdateScheduler {
    @Value("${app.local.enabled}")
    private boolean isEnabled;
    private final NpssEnrolmentService npssEnrolmentService;

    @Scheduled(cron = "${app.local.scheduler}")
    public void scheduleTaskUsingCronExpression() {
        log.info("scheduler flag is : {}", isEnabled);
        if (isEnabled) {
            log.info("Scheduler started to update default accounts in : " + LocalDateTime.now());
            npssEnrolmentService.updateDefaultAccount(null, true);
        }
    }
}
