package com.mashreq.transfercoreservice.client;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.notification.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "notification",
        url = "${app.services.notification}",
        configuration = FeignConfig.class)
public interface NotificationClient {

        @PostMapping(value = "/common/notification/sms", consumes = "application/json")
        SMSResponse sendSMS(@RequestHeader Map<String, String> headerMap, @RequestBody SMSObject smsObj);

        @PostMapping(value = "/common/message/sendNotification", consumes = "application/json")
        NotificationResponse sendPushNotification(@RequestHeader Map<String, String> headerMap, @RequestBody NotificationRequest notificationRequest, @RequestHeader("X-USSM-USER-CIF") String cif);

}
