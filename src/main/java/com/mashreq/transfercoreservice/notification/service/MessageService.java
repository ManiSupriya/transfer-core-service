package com.mashreq.transfercoreservice.notification.service;

public interface MessageService<T, R> {

    R sendMessage(T request);
}
