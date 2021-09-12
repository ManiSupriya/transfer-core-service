package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import freemarker.template.TemplateException;

import java.io.IOException;

public interface PostTransactionActivity<P> {

    void execute(P payload, RequestMetaData requestMetaData) throws IOException, TemplateException;
}
