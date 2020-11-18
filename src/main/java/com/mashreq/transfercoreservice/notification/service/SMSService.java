package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.logcore.annotations.TrackExecTimeAndResult;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.config.notification.SMSConfig;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.SMSObject;
import com.mashreq.transfercoreservice.notification.model.SMSResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;

import java.util.Arrays;
import java.util.Collection;


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
    private static final String MOBILE = "MOBILE";
    private static final String WEB = "WEB";
    private static final String NEOMOBILE = "NEOMOBILE";
    private static final String NEOWEB = "NEOWEB";
    private static final Collection<String> MASHREQ_RETAIL_CHANNELS = CollectionUtils.unmodifiableCollection(Arrays.asList(MOBILE, WEB));
    private static final Collection<String> MASHREQ_NEO_CHANNELS = CollectionUtils.unmodifiableCollection(Arrays.asList(NEOMOBILE, NEOWEB));
    /**
     * service method to prepare sms template based on type of sms and send sms
     */

    @TrackExecTimeAndResult
    public boolean sendSMS(CustomerNotification customerNotification, String type, RequestMetaData metaData, int retryCount, String phoneNo) {
        String logPrefix = metaData.getPrimaryCif() + ", retryCount: " + retryCount;
        String message =  smsConfig.getSMSTemplate(type,customerNotification);
        return sendSMS(message, phoneNo, metaData, logPrefix);
    }

    /**
     * prepares sms request and calls sms notification endpoint
     *
     * @param message
     * @param phoneNumber
     * @return
     */
    private boolean sendSMS(String message, String phoneNumber, RequestMetaData metaData, String logPrefix) {

        log.info("{}, smsMessage: {}, phoneNumber: {}. SMS being sent.", htmlEscape(logPrefix), htmlEscape(message), htmlEscape(phoneNumber));
        SMSResponse smsResponse = notificationClient.sendSMS(createSmsObject(message, phoneNumber, metaData));
        log.info("{}, smsResponse: {}, SMS response received.", htmlEscape(logPrefix), htmlEscape(smsResponse));
        return smsResponse != null && smsResponse.getStatusCode() != null && "SUCCESS".equalsIgnoreCase(smsResponse.getStatusCode());
    }

    /**
     * prepares and returns sms request
     *
     * @param message
     * @param phoneNumber
     * @return
     */
    private SMSObject createSmsObject(String message, String phoneNumber, RequestMetaData metaData) {
		SMSObject smsObject = new SMSObject();
		smsObject.setMessage(message);
		smsObject.setMobileNumber(phoneNumber);
		if (MASHREQ_RETAIL_CHANNELS.contains(metaData.getChannel())) {
			smsObject.setServiceId(smsConfig.getMashreqServiceId());
		} else if (MASHREQ_NEO_CHANNELS.contains(metaData.getChannel())) {
			smsObject.setServiceId(smsConfig.getNeoServiceId());
		} else {
			smsObject.setServiceId(smsConfig.getServiceId());
		}
		smsObject.setPriority(smsConfig.getPriority());
		return smsObject;
    }
}