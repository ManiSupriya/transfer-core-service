package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;

public interface MessageService<T, R> {

    R sendMessage(T request, RequestMetaData requestMetaData);
}
