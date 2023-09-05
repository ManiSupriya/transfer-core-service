package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.events.publisher.AuditEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.notification.model.EmailResponse;
import com.mashreq.transfercoreservice.notification.model.SendEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EMAIL_NOTIFICATION_FAILED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.EMAIL_NOTIFICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements MessageService<SendEmailRequest, EmailResponse> {

    private final NotificationClient notificationClient;
    private final AuditEventPublisher auditEventPublisher;

    @Override
    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public EmailResponse sendMessage(SendEmailRequest emailRequest, RequestMetaData requestMetaData) {
        try {
            ResponseEntity<EmailResponse> emailResponse = notificationClient.sendEmail(emailRequest);
            if (HttpStatus.OK != emailResponse.getStatusCode()) {
                log.error("[EmailServiceImpl] - Unable to send email. Notification service returned FAILURE. Request = {}, Response = {}",
                        htmlEscape(emailRequest), htmlEscape(emailResponse));
                auditEventPublisher.publishFailureEvent(EMAIL_NOTIFICATION, requestMetaData, "sending email notification failed", EMAIL_NOTIFICATION_FAILED.getCustomErrorCode(), EMAIL_NOTIFICATION_FAILED.getErrorMessage(), "Email notification failed");
            }
            log.info("[EmailServiceImpl] - Email sent successfully to address: {}", htmlEscape(emailRequest.getToEmailAddress()));
            auditEventPublisher.publishEventLifecycle(() -> emailResponse,
                    EMAIL_NOTIFICATION, requestMetaData, "email sent to " + Objects.requireNonNull(emailResponse.getBody()).getToEmailAddress());
            return emailResponse.getBody();
        } catch (Exception ex) {
            log.error("[EmailServiceImpl] - Error while calling notification service.", ex);
            auditEventPublisher.publishFailureEvent(EMAIL_NOTIFICATION, requestMetaData, "Email notification failed", EMAIL_NOTIFICATION_FAILED.getCustomErrorCode(), EMAIL_NOTIFICATION_FAILED.getErrorMessage(), ex.getMessage());
            GenericExceptionHandler.handleError(TransferErrorCode.EMAIL_NOTIFICATION_FAILED, TransferErrorCode.EMAIL_NOTIFICATION_FAILED.getErrorMessage());
        }
        return null;
    }


}
