package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SMS_NOTIFICATION_FAILED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.SMS_NOTIFICATION;


/**
 * async service used to send notification
 *
 * @author PallaviG
 */
@Slf4j
@Service("asyncNotificationService")
public class NotificationService {

    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AsyncUserEventPublisher userEventPublisher;

    @Async("GenericAsyncExecutor")
    @TrackExecTimeAndResult
    public void sendNotifications(CustomerNotification customer, String type, RequestMetaData metaData) {

        String phoneNo = metaData.getMobileNUmber();
        if (!StringUtils.isEmpty(phoneNo)) {
            sendSms(customer, type, metaData, phoneNo);
        }

        sendEmail(customer, type, metaData);

       userEventPublisher.publishSuccessEvent(SMS_NOTIFICATION, metaData, customer.getTxnRef() + " smsSent");
    }

    /**
     * sends sms to given phoneNumber using sms service
     */
    private void sendSms(CustomerNotification customer, String type, RequestMetaData metaData, String phoneNo) {
        if (NotificationType.OWN_ACCOUNT_TRANSACTION.equals(type)) {
            boolean smsSent = false;
            int smsRetryCount = 0;

            for (int i = 0; i < 3 && StringUtils.isNotBlank(phoneNo); i++) {
                try {
                    smsSent = smsService.sendSMS(customer, type, metaData.getPrimaryCif(), smsRetryCount, phoneNo);
                    if (smsSent) {
                        break;
                    }
                } catch (Exception e) {
                    userEventPublisher.publishFailureEvent(SMS_NOTIFICATION, metaData, "sending sms notification failed", SMS_NOTIFICATION_FAILED.getCustomErrorCode(), SMS_NOTIFICATION_FAILED.getErrorMessage(), e.getMessage());
                    GenericExceptionHandler.logOnly(e, "ErrorCode=" + SMS_NOTIFICATION_FAILED.getCustomErrorCode() + ",ErrorMessage=" + SMS_NOTIFICATION_FAILED.getErrorMessage() + ", " + ",type =" + type + ",Error in sendSMS() ," + metaData.getPrimaryCif() + ", ExceptionMessage=" + e.getMessage());
                    smsRetryCount++;
                }
            }
            log.info("{}, type={}, hasSmsSent={}, phoneNumber={}, retryCount={}, Status of sms sent",
                    htmlEscape(metaData.getPrimaryCif()), htmlEscape(type), htmlEscape(String.valueOf(smsSent)), htmlEscape(phoneNo), htmlEscape(String.valueOf(smsRetryCount)));
        }
    }

    private void sendEmail(CustomerNotification customer, String type, RequestMetaData metaData) {
        if (NotificationType.TRANSFER_SUCCESS_SEND.equals(type) && !StringUtils.isEmpty(metaData.getEmail())) {

            boolean emailSent = false;

            int emailRetryCount = 0;

            for (int i = 0; i < 3; i++) {
                try {
                    emailSent = emailService.sendEmail(customer, type, metaData, emailRetryCount);
                    if (emailSent) {
                        break;
                    }
                } catch (Exception e) {
//                    userEventPublisher.publishFailureEvent(AuditEventType.M2M_NOTIFICATION, metaData, "sending email notification failed", ErrorCodeSet.M2M1004.name(), ErrorCodeSet.M2M1004.getErrorDesc(), e.getMessage());
//                    GenericExceptionHandler.logOnly(e, "ErrorCode=" + ErrorCodeSet.M2M1004.name() + ",ErrorMessage=" + ErrorCodeSet.M2M1004.getErrorDesc() + ", " + ",type =" + type + ",Error in sendEmail() ," + metaData.getPrimaryCif() + ", ExceptionMessage=" + e.getMessage());
                    emailRetryCount++;
                }
            }
            log.info("{}, type={}, hasEmailSent={}, email={}, retryCount={}, Status of email sent",
                    htmlEscape(metaData.getPrimaryCif()), htmlEscape(type), htmlEscape(String.valueOf(emailSent)), htmlEscape(metaData.getEmail()), htmlEscape(String.valueOf(emailRetryCount)));
        }
    }

//    private void sendPushNotification(CustomerNotification customer, String type, RequestMetaData metaData, String phoneNo, DigitalUser digitalUser) {
//        if (NotificationType.REGISTER_SEND.equals(type) || NotificationType.TRANSFER_SUCCESS_SEND.equals(type) ||
//                NotificationType.TRANSFER_FAILED_SEND.equals(type)) {
//            try {
//                boolean pushMsgSent = pushNotification.sendPushNotification(customer, type, metaData, phoneNo, digitalUser);
//                customer.setSentPushNotification(pushMsgSent);
//            } catch (Exception e) {
//                userEventPublisher.publishFailureEvent(AuditEventType.M2M_NOTIFICATION, metaData, "sending push notification failed", ErrorCodeSet.M2M1004.name(), ErrorCodeSet.M2M1004.getErrorDesc(), e.getMessage());
//                GenericExceptionHandler.logOnly(e, "ErrorCode=" + ErrorCodeSet.M2M1004.name() + ",ErrorMessage=" + ErrorCodeSet.M2M1004.getErrorDesc() + ", " + ",type =" + type + ",Error in pushNotification() ," + metaData.getPrimaryCif() + ", ExceptionMessage=" + e.getMessage());
//            }
//        }
//    }


}
