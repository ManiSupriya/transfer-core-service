package com.mashreq.transfercoreservice.event.mapper;


import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.event.entity.UserEventAudit;
import com.mashreq.transfercoreservice.event.model.AuditEvent;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditEventMapper {

    public static final String SYSTEM = "SYSTEM";
    private static final int DESCRIPTION_COLUMN_MAX_LENGTH = 500;
    private static final int DETAILS_COLUMN_MAX_LENGTH = 200;

    public AuditEvent createAuditEvent(final EventType eventType, final EventStatus status, final RequestMetaData billPaymentMetaData, final String remarks,
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
                .clientIp(billPaymentMetaData.getDeviceIP())
                .mwSrcMsgId(mwSrcMsgId)
                .build();
    }

    public UserEventAudit createEntity(AuditEvent auditEvent) {
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
        userEventAudit.setErrorDescription(trimString(auditEvent.getErrorMessage(), DESCRIPTION_COLUMN_MAX_LENGTH));
        userEventAudit.setErrorDetails(trimString(auditEvent.getErrorDetails(), DETAILS_COLUMN_MAX_LENGTH));
        userEventAudit.setClientIp(auditEvent.getClientIp());
        userEventAudit.setRegion(auditEvent.getRegion());
        userEventAudit.setRemarks(trimString(auditEvent.getRemarks(), DESCRIPTION_COLUMN_MAX_LENGTH));
        return userEventAudit;
    }

    private String trimString(String inputString, int length) {
        return StringUtils.length(inputString) > length
                ? StringUtils.substring(inputString, length)
                : inputString;
    }


}
