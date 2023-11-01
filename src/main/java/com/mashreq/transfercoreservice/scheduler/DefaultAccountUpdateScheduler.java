package com.mashreq.transfercoreservice.scheduler;

import com.mashreq.transfercoreservice.fundtransfer.service.NpssEnrolmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAccountUpdateScheduler {
    @Value("${app.local.enabled}")
    private boolean isEnabled;

    @Value("${app.local.consentSize}")
    private int rowSize;

    @Value("${app.local.peakHoursStartTime}")
    private String peakHoursStartTime;
    @Value("${app.local.peakHoursEndTime}")
    private String peakHoursEndTime;
    @Value("${app.local.peakHoursNoOfRecords}")
    private Integer peakHoursNoOfRecords;

    @Value("${app.local.nonPeakHoursStartTime}")
    private String nonPeakHoursStartTime;
    @Value("${app.local.nonPeakHoursEndTime}")
    private String nonPeakHoursEndTime;
    @Value("${app.local.nonPeakHoursNoOfRecords}")
    private Integer nonPeakHoursNoOfRecords;
    @Value("${app.local.eodNoOfRecords}")
    private Integer eodNoOfRecords;
    private final NpssEnrolmentService npssEnrolmentService;

    @Scheduled(cron = "${app.local.scheduler}")
   @SchedulerLock(name = "ScheduledTask_scheduleDefaultAccountUpdate",
            lockAtLeastFor = "${app.local.lockAtLeastFor}", lockAtMostFor = "${app.local.lockAtMostFor}")
    public void scheduleTaskUsingCronExpression() {
        log.info("scheduler flag is : {}", isEnabled);
        if (isEnabled) {
            log.info("Scheduler started to update default accounts in : " + LocalDateTime.now());
            npssEnrolmentService.updateDefaultAccount(null, true,getRowSize());
        }
    }

    private Integer getRowSize() {
        log.info("DefaultAccountUpdateScheduler >> getRowSize >> ");
        LocalTime localTime = LocalTime.parse(LocalTime.now().toString().substring(0,5),
                DateTimeFormatter.ofPattern("HH:mm"));
        log.info("DefaultAccountUpdateScheduler >> getRowSize >> current local time >> {}",localTime);
        // Peak Hours
        if(localTime.isAfter(LocalTime.parse(peakHoursStartTime))
                && localTime.isBefore(LocalTime.parse(peakHoursEndTime))) {
            log.info("DefaultAccountUpdateScheduler >> getRowSize >> Falling under peak hours >> {}{}{}"
                    ,peakHoursStartTime,peakHoursEndTime,peakHoursNoOfRecords);
            return peakHoursNoOfRecords;
        }
        // Non-Peak Hours
        else if (localTime.isAfter(LocalTime.parse(nonPeakHoursStartTime))
                && localTime.isBefore(LocalTime.parse(nonPeakHoursEndTime))){
            log.info("DefaultAccountUpdateScheduler >> getRowSize >> Falling under non peak hours >> {}{}{}"
                    ,nonPeakHoursStartTime,nonPeakHoursEndTime,nonPeakHoursNoOfRecords);
            return nonPeakHoursNoOfRecords;
        }
        // EOD Hours
        log.info("DefaultAccountUpdateScheduler >> getRowSize >> Falling under eod hours >> {}",eodNoOfRecords);
        return eodNoOfRecords;
    }
}
