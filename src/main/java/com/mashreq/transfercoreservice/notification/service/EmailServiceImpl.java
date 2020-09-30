package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.config.notification.EmailTemplateHelper;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.notification.model.EmailRequest;
import com.mashreq.transfercoreservice.notification.model.EmailResponse;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements MessageService<EmailRequest, EmailResponse> {

    private final NotificationClient notificationClient;
    private final EmailTemplateHelper emailTemplateHelper;

    @Override
    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public EmailResponse sendMessage(EmailRequest emailRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<EmailResponse> emailResponse = notificationClient.sendEmail(emailRequest);
            if (HttpStatus.OK != emailResponse.getStatusCode()) {
                log.error("[EmailServiceImpl] - Unable to send email. Notification service returned FAILURE. Request = {}, Response = {}",
                        htmlEscape(emailRequest), htmlEscape(emailResponse));
                //GenericExceptionHandler.handleError(EXTERNAL_NOTIFICATION_SERVICE_FAILED, EXTERNAL_NOTIFICATION_SERVICE_FAILED.getErrorMessage());
            }
            log.info("[EmailServiceImpl] - Email sent successfully to address: {}", htmlEscape(emailRequest.getToEmailAddress()));
            return emailResponse.getBody();
        } catch (Exception ex) {
            log.error("[EmailServiceImpl] - Error while calling notification service.", ex);
            GenericExceptionHandler.handleError(TransferErrorCode.EMAIL_NOTIFICATION_FAILED, TransferErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorMessage());
        }
        return null;
    }

    public EmailRequest prepareEmailRequest(SendEmailRequest sendEmailRequest) throws IOException, TemplateException {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setFromEmailAddress(sendEmailRequest.getFromEmailAddress());
        emailRequest.setFromEmailName(sendEmailRequest.getFromEmailName());
        emailRequest.setToEmailAddress(sendEmailRequest.getToEmailAddress());
        emailRequest.setSubject(sendEmailRequest.getSubject());
        String emailBody = emailTemplateHelper.getEmailTemplate(sendEmailRequest.getTemplateName(), sendEmailRequest.getTemplateKeyValues());
        emailRequest.setText(emailBody);
       return emailRequest;
    }



}
