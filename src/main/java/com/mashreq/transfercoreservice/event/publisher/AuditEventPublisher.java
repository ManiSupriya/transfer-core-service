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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

/**
 * This class should be used to publish any kind in Audit Events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher implements Publisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditEventMapper auditEventMapper;
    private final EventProperties eventProperties;

    /**
     * @param eventType
     * @param eventStatus
     * @param metaData
     * @param remarks
     */
    @Override
    public void publishEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, eventStatus, metaData, remarks, null, null, null, null);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }


    }

    private boolean isNotIgnoredEvent(EventType eventType) {
        Map<String, String> map = eventProperties.getMap();
        if (map == null || map.size() == 0) {
            return true;
        }
        return !"ignore".equalsIgnoreCase(eventProperties.getMap().get(eventType.name()));
    }

    /**
     * @param eventType
     * @param eventStatus
     * @param metaData
     * @param remarks
     * @param errorCode
     * @param errorDesc
     * @param errorDetails
     */
    @Override
    public void publishEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks,
                             final String errorCode, final String errorDesc, final String errorDetails) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, eventStatus, metaData, remarks, errorCode, errorDesc, errorDetails, null);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }

    }

    /**
     * @param eventType
     * @param eventStatus
     * @param metaData
     * @param remarks
     * @param mwSrcMsgId
     * @param errorCode
     * @param errorDesc
     * @param errorDetails
     */
    @Override
    public void publishEsbEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks,
                                final String mwSrcMsgId, final String errorCode, final String errorDesc, final String errorDetails) {

        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, eventStatus, metaData, remarks, errorCode, errorDesc, errorDetails, mwSrcMsgId);
            log.info("Publishing event = {} ", auditEvent);
            applicationEventPublisher.publishEvent(auditEvent);
        }

    }

    /**
     * @param eventType
     * @param eventStatus
     * @param metaData
     * @param remarks
     * @param mwSrcMsgId
     */
    @Override
    public void publishEsbEvent(final EventType eventType, final EventStatus eventStatus, final RequestMetaData metaData, final String remarks,
                                final String mwSrcMsgId) {
        if (isNotIgnoredEvent(eventType)) {
            AuditEvent auditEvent = auditEventMapper.createAuditEvent(eventType, eventStatus, metaData, remarks, null, null, null, mwSrcMsgId);
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
            publishEvent(eventType, EventStatus.STARTED, metaData, remarks);
            final T response = function.get();
            publishEvent(eventType, EventStatus.SUCCESS, metaData, remarks);
            return response;
        } catch (GenericException genericException) {
            publishEvent(eventType, EventStatus.FAILURE, metaData, remarks,
                    genericException.getErrorCode(), genericException.getMessage(), genericException.getErrorDetails());
            throw genericException;
        } catch (Exception exception) {
            publishEvent(eventType, EventStatus.FAILURE, metaData, remarks,
                    INTERNAL_ERROR.getCustomErrorCode(), INTERNAL_ERROR.getErrorMessage(), exception.getMessage());
            throw exception;
        }
    }


}
