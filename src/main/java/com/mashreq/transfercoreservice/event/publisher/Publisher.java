package com.mashreq.transfercoreservice.event.publisher;

import com.mashreq.transfercoreservice.event.model.EventStatus;
import com.mashreq.transfercoreservice.event.model.EventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;

import java.util.function.Supplier;

public interface Publisher {

    void publishEvent(EventType eventType, EventStatus eventStatus, RequestMetaData metaData, String remarks);

    void publishEvent(EventType eventType, EventStatus eventStatus, RequestMetaData metaData, String remarks,
                      String errorCode, String errorDesc, String errorDetails);

    void publishEsbEvent(EventType eventType, EventStatus eventStatus, RequestMetaData metaData, String remarks,
                         String mwSrcMsgId, String errorCode, String errorDesc, String errorDetails);

    void publishEsbEvent(EventType eventType, EventStatus eventStatus, RequestMetaData metaData, String remarks,
                         String mwSrcMsgId);

    <T> T publishEventLifecycle(Supplier<T> function, EventType eventType, RequestMetaData metaData, String remarks);
}
