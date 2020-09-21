package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.notification.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "notification",
        url = "${app.services.notification}",
        configuration = FeignConfig.class)
public interface NotificationClient {

        @PostMapping(value = "/common/notification/sms", consumes = "application/json")
        SMSResponse sendSMS(@RequestBody SMSObject smsObj);

        @PostMapping(value = "/common/notification/email", consumes = "application/json")
        ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest emailRequest);

        @PostMapping(value = "/common/message/sendNotification", consumes = "application/json")
        NotificationResponse sendPushNotification(@RequestBody NotificationRequest notificationRequest, @RequestHeader("X-USSM-USER-CIF") String cif);

}
