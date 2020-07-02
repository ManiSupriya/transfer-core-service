package com.mashreq.transfercoreservice.event.publisher;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.event.mapper.AuditEventMapper;
import com.mashreq.transfercoreservice.event.model.AuditEvent;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final AuditEventMapper auditEventMapper;

    public void publishEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks) {
        AuditEvent auditEvent = auditEventMapper.map(eventType, eventStatus, metaData, remarks, null, null, null, null);
        log.info("Publishing event = {} ", auditEvent);
        applicationEventPublisher.publishEvent(auditEvent);
    }

    public void publishEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks,
                             final String errorCode, final String errorDesc, final String errorDetails) {
        AuditEvent auditEvent = auditEventMapper.map(eventType, eventStatus, metaData, remarks, errorCode, errorDesc, errorDetails, null);
        log.info("Publishing event = {} ", auditEvent);
        applicationEventPublisher.publishEvent(auditEvent);
    }

    public void publishEsbEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks,
                                final String mwSrcMsgId, final String errorCode, final String errorDesc, final String errorDetails) {
        AuditEvent auditEvent = auditEventMapper.map(eventType, eventStatus, metaData, remarks, errorCode, errorDesc, errorDetails, mwSrcMsgId);
        log.info("Publishing event = {} ", auditEvent);
        applicationEventPublisher.publishEvent(auditEvent);
    }

    public void publishEsbEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks,
                                final String mwSrcMsgId) {
        AuditEvent auditEvent = auditEventMapper.map(eventType, eventStatus, metaData, remarks, null, null, null, mwSrcMsgId);
        log.info("Publishing event = {} ", auditEvent);
        applicationEventPublisher.publishEvent(auditEvent);
    }

    public <T> T publishEventLifecycle(Supplier<T> function, final EventType eventType, final RequestMetaData metaData, final String remarks) {
        try {
            final T response = function.get();
            publishEvent(eventType, EventStatus.SUCCESS, metaData, remarks);
            return response;
        } catch (GenericException genericException) {
            publishEvent(eventType, EventStatus.FAILURE, metaData, remarks,
                    genericException.getErrorCode(), genericException.getMessage(),genericException.getErrorDetails());
            throw genericException;
        } catch (Exception exception) {
            publishEvent(eventType, EventStatus.FAILURE, metaData, remarks,
                    INTERNAL_ERROR.getCustomErrorCode(), INTERNAL_ERROR.getErrorMessage(), exception.getMessage());
            throw exception;
        }
    }


}
