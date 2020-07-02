package com.mashreq.transfercoreservice.event.listener;


import com.mashreq.transfercoreservice.event.mapper.AuditEventMapper;
import com.mashreq.transfercoreservice.event.model.AuditEvent;
import com.mashreq.transfercoreservice.event.repository.UserEventAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncAuditEventListener implements ApplicationListener<AuditEvent> {


    private final UserEventAuditRepository userEventAuditRepository;
    private final AuditEventMapper auditEventMapper;

    @Override
    public void onApplicationEvent(AuditEvent auditEvent) {
        log.info("Audit Event raised = {} ", auditEvent);
        userEventAuditRepository.save(auditEventMapper.map(auditEvent));
    }



}
