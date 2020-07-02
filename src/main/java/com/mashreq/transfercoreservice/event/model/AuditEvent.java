package com.mashreq.transfercoreservice.event.model;


import lombok.*;
import org.springframework.context.ApplicationEvent;



@Getter
@ToString
public class AuditEvent extends ApplicationEvent {

    private EventType eventType;
    private EventStatus status;
    private String userId;
    private String cif;
    private String channel;
    private String correlationId;
    private String userCacheKey;
    private String mwSrcMsgId;
    private String actionKey;
    private EventSink eventSink;
    private String errorCode;
    private String errorDetails;
    private String errorMessage;
    private String clientIp;
    private String region;
    private String remarks;

    @Builder(toBuilder = true)
    public AuditEvent(Object source, EventType eventType, EventStatus status,
                      String userId, String cif, String channel, String correlationId,
                      String userCacheKey, String mwSrcMsgId, String actionKey,
                      String errorCode, String errorDetails, String errorMessage, String clientIp, String region,
                      String remarks) {
        super(source);
        this.eventType = eventType;
        this.status = status;
        this.userId = userId;
        this.cif = cif;
        this.channel = channel;
        this.correlationId = correlationId;
        this.userCacheKey = userCacheKey;
        this.mwSrcMsgId = mwSrcMsgId;
        this.actionKey = actionKey;
        this.eventSink = EventSink.DATABASE;
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
        this.errorMessage = errorMessage;
        this.region = region;
        this.clientIp = clientIp;
        this.remarks = remarks;
    }
}
