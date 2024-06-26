package com.mashreq.transfercoreservice.notification.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.NotificationClient;
import com.mashreq.transfercoreservice.client.RequestMetadataMapper;
import com.mashreq.transfercoreservice.config.notification.PushTemplate;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.notification.model.CustomerNotification;
import com.mashreq.transfercoreservice.notification.model.DeviceInfo;
import com.mashreq.transfercoreservice.notification.model.NotificationRequest;
import com.mashreq.transfercoreservice.notification.model.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.mashreq.mobcommons.services.CustomHtmlEscapeUtil.htmlEscape;

@Service
@Slf4j
public class PushNotificationImpl {

    @Autowired
    NotificationClient notificationClient;

    @Autowired
    PushTemplate pushTemplate;

    boolean sendPushNotification(CustomerNotification customer, String type, RequestMetaData metaData,UserDTO userDTO) {
        if (StringUtils.isNotBlank(userDTO.getDeviceRegisteredForPush())) {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setChannelId(metaData.getChannel());
            notificationRequest.setDeviceInfo(DeviceInfo.valueOf(userDTO.getDeviceInfo()));
            String message = populateMessage(customer, type);
            notificationRequest.setMessage(message);
            Map<String,String> headerMap = RequestMetadataMapper.collectRequestMetadataAsMap(metaData);
            NotificationResponse notificationResponse = notificationClient.sendPushNotification(headerMap, notificationRequest, metaData.getPrimaryCif());
            log.info("{}, pushNotificationResponse: {}, Push notification response received.", htmlEscape(metaData), htmlEscape(notificationResponse));
            return notificationResponse != null && notificationResponse.getNotificationId()!=null && notificationResponse.getTrackingId()!=null;
        }
        return false;
    }

    private String populateMessage(CustomerNotification customerNotification,String type) {
        return pushTemplate.getPushTemplate(type, customerNotification);
    }
}
