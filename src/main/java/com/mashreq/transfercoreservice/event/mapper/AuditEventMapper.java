package com.mashreq.transfercoreservice.event.mapper;


import com.mashreq.transfercoreservice.event.entity.UserEventAudit;
import com.mashreq.transfercoreservice.event.model.AuditEvent;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditEventMapper {

    public static final String SYSTEM = "SYSTEM";

    public AuditEvent map(final EventType eventType, final EventStatus status, final RequestMetaData billPaymentMetaData, final String remarks,
                          final String errorCode, final String errorDesc, final String errorDetails, final String mwSrcMsgId) {
        log.info("Preparing AuditEvent for event: {} , channelTraceId: {} ", eventType, billPaymentMetaData.getChannelTraceId());
        return AuditEvent.builder()
                .source(this)
                .eventType(eventType)
                .status(status)
                .actionKey(billPaymentMetaData.getActionKey())
                .channel(billPaymentMetaData.getChannel())
                .correlationId(billPaymentMetaData.getCoRelationId())
                .userCacheKey(billPaymentMetaData.getUserCacheKey())
                .userId(billPaymentMetaData.getUsername())
                .cif(billPaymentMetaData.getPrimaryCif())
                .errorCode(errorCode)
                .errorMessage(errorDesc)
                .errorDetails(errorDetails)
                .region(billPaymentMetaData.getRegion())
                .remarks(remarks)
                .clientIp(billPaymentMetaData.getIp())
                .mwSrcMsgId(mwSrcMsgId)
                .build();
    }

    public UserEventAudit map(AuditEvent auditEvent) {
        log.info("Preparing UserEventAudit entity for event: {} , correlationId: {} ", auditEvent.getEventType(), auditEvent.getCorrelationId());
        UserEventAudit userEventAudit = new UserEventAudit();
        userEventAudit.setActionKey(auditEvent.getActionKey());
        userEventAudit.setChannel(auditEvent.getChannel());
        userEventAudit.setCorrelationId(auditEvent.getCorrelationId());
        userEventAudit.setCreatedBy(SYSTEM);
        userEventAudit.setUsername(auditEvent.getUserId());
        userEventAudit.setSessionCacheKey(auditEvent.getUserCacheKey());
        userEventAudit.setStatus(auditEvent.getStatus().name());
        userEventAudit.setCif(auditEvent.getCif());
        userEventAudit.setMwSrcMsgId(auditEvent.getMwSrcMsgId());
        userEventAudit.setEventCategory(auditEvent.getEventType().getType());
        userEventAudit.setEventName(auditEvent.getEventType().name());
        userEventAudit.setErrorCode(auditEvent.getErrorCode());
        userEventAudit.setErrorDescription(auditEvent.getErrorMessage());
        userEventAudit.setErrorDetails(auditEvent.getErrorDetails());
        userEventAudit.setClientIp(auditEvent.getClientIp());
        userEventAudit.setRegion(auditEvent.getRegion());
        userEventAudit.setRemarks(auditEvent.getRemarks());
        return userEventAudit;
    }


}
