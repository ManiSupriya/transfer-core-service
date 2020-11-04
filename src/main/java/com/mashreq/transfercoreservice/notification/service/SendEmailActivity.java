package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.transfercoreservice.notification.model.EmailRequest;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailActivity implements PostTransactionActivity<SendEmailRequest> {

    private final EmailServiceImpl emailService;

    @Override
    @Async("generalTaskExecutor")
    public void execute(SendEmailRequest payload) throws IOException, TemplateException {
        if (payload.isEmailPresent()) {
            final EmailRequest emailRequest = emailService.prepareEmailRequest(payload);
            emailService.sendMessage(emailRequest);
            log.info("[SendEmailActivity] - Email sent to the customer on following emailAddress: {}", htmlEscape(payload.getToEmailAddress()));
        }

    }
}
