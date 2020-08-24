package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.config.SMSConfig;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.SMSObject;
import com.mashreq.transfercoreservice.notification.model.SMSResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;


/**
 * This class sends sms notifications for payment successfully
 * placed to middleware as well as rejected cases.
 *
 * @author PallaviG
 */
@Slf4j
@Service
public class SMSService  {

    @Autowired
    NotificationClient notificationClient;

    @Autowired
    SMSConfig smsConfig;
    /**
     * service method to prepare sms template based on type of sms and send sms
     */

    @TrackExecTimeAndResult
    public boolean sendSMS(CustomerNotification customerNotification, String type, String logPrefix, int retryCount, String phoneNo) {
        logPrefix = logPrefix + ", retryCount: " + retryCount;
        String message =  smsConfig.getSMSTemplate(type,customerNotification);
        return sendSMS(message, phoneNo, logPrefix);
    }

    /**
     * prepares sms request and calls sms notification endpoint
     *
     * @param message
     * @param phoneNumber
     * @return
     */
    private boolean sendSMS(String message, String phoneNumber, String logPrefix) {

        log.info("{}, smsMessage: {}, phoneNumber: {}. SMS being sent.", htmlEscape(logPrefix), htmlEscape(message), htmlEscape(phoneNumber));
        SMSResponse smsResponse = notificationClient.sendSMS(createSmsObject(message, phoneNumber));
        log.info("{}, smsResponse: {}, SMS response received.", htmlEscape(logPrefix), htmlEscape(smsResponse.toString()));
        return smsResponse != null && smsResponse.getStatusCode() != null && "SUCCESS".equalsIgnoreCase(smsResponse.getStatusCode());
    }

    /**
     * prepares and returns sms request
     *
     * @param message
     * @param phoneNumber
     * @return
     */
    private SMSObject createSmsObject(String message, String phoneNumber) {
        SMSObject smsObject = new SMSObject();
        smsObject.setMessage(message);
        smsObject.setMobileNumber(phoneNumber);
        smsObject.setPriority(smsConfig.getPriority());
        smsObject.setServiceId(smsConfig.getServiceId());
        return smsObject;
    }
}