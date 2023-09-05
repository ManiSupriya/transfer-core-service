package com.mashreq.transfercoreservice.notification.service;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;

import java.io.IOException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailActivity implements PostTransactionActivity<SendEmailRequest> {

    private final EmailServiceImpl emailService;

    @Override
    @Async("generalTaskExecutor")
    public void execute(SendEmailRequest payload, RequestMetaData requestMetaData) throws IOException, TemplateException {
        if (payload.isEmailPresent()) {
            emailService.sendMessage(payload, requestMetaData);
            log.info("[SendEmailActivity] - Email sent to the customer on following emailAddress: {}", htmlEscape(payload.getToEmailAddress()));
        }

    }
}
