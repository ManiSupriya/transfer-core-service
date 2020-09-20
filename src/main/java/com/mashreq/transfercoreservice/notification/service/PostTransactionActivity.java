package com.mashreq.transfercoreservice.notification.service;

import freemarker.template.TemplateException;

import java.io.IOException;

public interface PostTransactionActivity<P> {

    void execute(P payload) throws IOException, TemplateException;
}
