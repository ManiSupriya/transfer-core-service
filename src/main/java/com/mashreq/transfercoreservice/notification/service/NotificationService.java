package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.PUSH_NOTIFICATION_FAILED;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SMS_NOTIFICATION_FAILED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.PUSH_NOTIFICATION;
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
    private PushNotificationImpl pushNotification;

    @Autowired
    private AsyncUserEventPublisher userEventPublisher;
    
    @Autowired
    private DigitalUserSegment digitalUserSegment;

    public static final String MOBILE = "MOBILE";
    public static final String MOBILE_BANKING = "Mobile Banking";
    public static final String ONLINE_BANKING = "Online Banking";

    @Async("GenericAsyncExecutor")
    @TrackExecTimeAndResult
    public void sendNotifications(CustomerNotification customer, String type, RequestMetaData metaData, UserDTO userDTO) {
        log.info("NotificationService >> sendNotifications >> Initiated for the cif{}{}",metaData.getPrimaryCif(),customer);
        String phoneNo = metaData.getMobileNUmber();
        boolean isMobile = metaData.getChannel().contains(MOBILE);
        String channel = isMobile ? MOBILE_BANKING : ONLINE_BANKING;
        customer.setChannel(channel);
        /**
         * BUG 36630 - Local & INFT - SMS - customer contact center number is not correct in SMS for fund transfer
         * 
         *  change: 
         * this is fix for the wrong customer care details in SMS, Now SMS Info populating based on the Segment from segment_ms table
         * 
         */
        customer.setSegment(digitalUserSegment.getCustomerCareInfo(metaData.getSegment()));
        if (!StringUtils.isEmpty(phoneNo)) {
            log.info("NotificationService >> sendSms >> Initiated for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
            sendSms(customer, type, metaData, phoneNo);
            log.info("NotificationService >> sendSms >> Completed for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
        }
        log.info("NotificationService >> sendPushNotification >> Initiated for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
        sendPushNotification(customer, type, metaData, phoneNo, userDTO);
        log.info("NotificationService >> sendPushNotification >> Completed for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
    }

    /**
     * sends sms to given phoneNumber using sms service
     */
    //can add conditions on type if sms is not required to be sent for the type
    private void sendSms(CustomerNotification customer, String type, RequestMetaData metaData, String phoneNo) {
            boolean smsSent = false;
            int smsRetryCount = 0;

            for (int i = 0; i < 3 && StringUtils.isNotBlank(phoneNo); i++) {
                try {
                    smsSent = smsService.sendSMS(customer, type, metaData, smsRetryCount, phoneNo);
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
            userEventPublisher.publishSuccessEvent(SMS_NOTIFICATION, metaData, customer.getTxnRef() + " smsSent");
    }

    private void sendPushNotification(CustomerNotification customer, String type, RequestMetaData metaData, String phoneNo, UserDTO userDTO) {
            try {
                boolean response  = pushNotification.sendPushNotification(customer, type, metaData, userDTO);
                if(response){
                    log.error("NotificationService >> sendPushNotification >> sending push notification Success event for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
                    userEventPublisher.publishSuccessEvent(PUSH_NOTIFICATION, metaData, customer.getTxnRef() + " pushSent");
                }
                else{
                    log.error("NotificationService >> sendPushNotification >> sending push notification failed for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
                    userEventPublisher.publishFailureEvent(PUSH_NOTIFICATION, metaData, "sending push notification failed", PUSH_NOTIFICATION_FAILED.getCustomErrorCode(), PUSH_NOTIFICATION_FAILED.getErrorMessage(), "push notification failed");
                }
            } catch (Exception e) {
                log.error("NotificationService >> sendPushNotification >> Error for the cif{}{}",metaData.getPrimaryCif(),phoneNo);
                userEventPublisher.publishFailureEvent(PUSH_NOTIFICATION, metaData, "sending push notification failed", PUSH_NOTIFICATION_FAILED.getCustomErrorCode(), PUSH_NOTIFICATION_FAILED.getErrorMessage(), e.getMessage());
                GenericExceptionHandler.logOnly(e, "ErrorCode=" + PUSH_NOTIFICATION_FAILED.getCustomErrorCode() + ",ErrorMessage=" + PUSH_NOTIFICATION_FAILED.getErrorMessage() + ", " + ",type =" + type + ",Error in pushNotification() ," + metaData.getPrimaryCif() + ", ExceptionMessage=" + e.getMessage());
            }
    }
}
