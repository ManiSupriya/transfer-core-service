package com.mashreq.transfercoreservice.event.publisher;

import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.event.model.EventType;

import java.util.function.Supplier;

public interface AsyncUserEventPublisher {

    /**
     * Publish success events
     * @param eventType
     * @param metaData
     * @param remarks
     */
    void publishSuccessEvent(EventType eventType, RequestMetaData metaData, String remarks);

    /**
     * Publish success events
     * @param eventType
     * @param metaData
     * @param remarks
     */
    void publishStartedEvent(EventType eventType, RequestMetaData metaData, String remarks);

    /**
     * Publish failure events
     * @param eventType
     * @param metaData
     * @param remarks
     * @param errorCode
     * @param errorDesc
     * @param errorDetails
     */
    void publishFailureEvent(EventType eventType, RequestMetaData metaData, String remarks,
                      String errorCode, String errorDesc, String errorDetails);

    /**
     * Publish failure events for  ESB call
     * @param eventType
     * @param metaData
     * @param remarks
     * @param mwSrcMsgId
     * @param errorCode
     * @param errorDesc
     * @param errorDetails
     */
    void publishFailedEsbEvent(EventType eventType, RequestMetaData metaData, String remarks,
                         String mwSrcMsgId, String errorCode, String errorDesc, String errorDetails);

    /**
     * Publish success events for  ESB call
     * @param eventType
     * @param metaData
     * @param remarks
     * @param mwSrcMsgId
     */
    void publishSuccessfulEsbEvent(EventType eventType, RequestMetaData metaData, String remarks,
                         String mwSrcMsgId);

    /**
     * Track Event's lifecycle and publish
     * @param function
     * @param eventType
     * @param metaData
     * @param remarks
     * @param <T>
     * @return
     */
    <T> T publishEventLifecycle(Supplier<T> function, EventType eventType, RequestMetaData metaData, String remarks);
}
