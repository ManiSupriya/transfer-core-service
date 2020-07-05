package com.mashreq.transfercoreservice.event.publisher;

import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.transfercoreservice.event.conf.EventProperties;
import com.mashreq.transfercoreservice.event.mapper.AuditEventMapper;
import com.mashreq.transfercoreservice.event.model.AuditEvent;
import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

/**
 * This class should be used to publish any kind in Audit Events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher implements AsyncUserEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditEventMapper auditEventMapper;
    private final EventProperties eventProperties;

    /**
     * @param eventType
     * @param metaData
     * @param remarks
     */
    @Override
    public void publishSuccessEvent(final EventType eventType, final RequestMetaData metaData, final String remarks) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, EventStatus.SUCCESS, metaData, remarks,
                    null, null, null, null);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }
    }


    /**
     * @param eventType
     * @param metaData
     * @param remarks
     */
    @Override
    public void publishStartedEvent(final EventType eventType, final RequestMetaData metaData, final String remarks) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, EventStatus.STARTED, metaData, remarks,
                    null, null, null, null);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }
    }


    /**
     * @param eventType
     * @param metaData
     * @param remarks
     * @param errorCode
     * @param errorDesc
     * @param errorDetails
     */
    @Override
    public void publishFailureEvent(final EventType eventType, final RequestMetaData metaData, final String remarks,
                             final String errorCode, final String errorDesc, final String errorDetails) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, EventStatus.FAILURE, metaData, remarks,
                    errorCode, errorDesc, errorDetails, null);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }

    }

    /**
     * @param eventType
     * @param metaData
     * @param remarks
     * @param mwSrcMsgId
     * @param errorCode
     * @param errorDesc
     * @param errorDetails
     */
    @Override
    public void publishFailedEsbEvent(final EventType eventType, final RequestMetaData metaData, final String remarks,
                                final String mwSrcMsgId, final String errorCode, final String errorDesc, final String errorDetails) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, EventStatus.FAILURE, metaData, remarks,
                    errorCode, errorDesc, errorDetails, mwSrcMsgId);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }

    }

    /**
     * @param eventType
     * @param metaData
     * @param remarks
     * @param mwSrcMsgId
     */
    @Override
    public void publishSuccessfulEsbEvent(final EventType eventType, final RequestMetaData metaData, final String remarks,
                                final String mwSrcMsgId) {
        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, EventStatus.SUCCESS, metaData, remarks,
                    null, null, null, mwSrcMsgId);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }

    }

    /**
     * @param function
     * @param eventType
     * @param metaData
     * @param remarks
     * @param <T>
     * @return
     */
    @Override
    public <T> T publishEventLifecycle(Supplier<T> function, final EventType eventType, final RequestMetaData metaData, final String remarks) {
        try {
            publishStartedEvent(eventType, metaData, remarks);
            final T response = function.get();
            publishSuccessEvent(eventType, metaData, remarks);
            return response;
        } catch (GenericException genericException) {
            publishFailureEvent(eventType, metaData, remarks,
                    genericException.getErrorCode(), genericException.getMessage(), genericException.getErrorDetails());
            throw genericException;
        } catch (Exception exception) {
            publishFailureEvent(eventType, metaData, remarks,
                    INTERNAL_ERROR.getCustomErrorCode(), INTERNAL_ERROR.getErrorMessage(), exception.getMessage());
            throw exception;
        }
    }


    private boolean isNotIgnoredEvent(EventType eventType) {
        Map<String, String> map = eventProperties.getMap();
        if (map == null || map.size() == 0) {
            return true;
        }
        return !"ignore".equalsIgnoreCase(eventProperties.getMap().get(eventType.name()));
    }

}
