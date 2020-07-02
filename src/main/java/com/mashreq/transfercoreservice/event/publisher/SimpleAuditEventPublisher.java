package com.mashreq.transfercoreservice.event.publisher;


import com.mashreq.transfercoreservice.event.mapper.AuditEventMapper;
import com.mashreq.transfercoreservice.event.repository.UserEventAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleAuditEventPublisher {

    private final UserEventAuditRepository userEventAuditRepository;
    private final AuditEventMapper auditEventMapper;

    /*@Async
    public void publishEvent(AuditEvent auditEvent) {
        log.info("Audit Event raised = {} ", auditEvent);
        try{
            userEventAuditRepository.save(auditEventMapper.map(auditEvent));
        }
        catch (Exception e){
            log.error("[SimpleAuditEventPublisher] Error while publishing events to audit table", e);
            throw e;
        }
    }*/

}
