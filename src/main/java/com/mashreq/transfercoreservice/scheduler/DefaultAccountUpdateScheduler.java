package com.mashreq.transfercoreservice.scheduler;

import com.mashreq.transfercoreservice.client.AccountClient;
import com.mashreq.transfercoreservice.fundtransfer.service.NpssEnrolmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAccountUpdateScheduler {

    private final NpssEnrolmentService npssEnrolmentService;

    @Scheduled(cron = "${app.local.scheduler}")
    public void scheduleTaskUsingCronExpression() {
        System.out.println("Scheduler started to update default accounts in : " + LocalDateTime.now());
        npssEnrolmentService.updateDefaultAccount(null,true);
    }
}
