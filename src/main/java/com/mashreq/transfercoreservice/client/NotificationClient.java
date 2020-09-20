package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.config.feign.FeignConfig;
import com.mashreq.transfercoreservice.notification.model.EmailRequest;
import com.mashreq.transfercoreservice.notification.model.EmailResponse;
import com.mashreq.transfercoreservice.notification.model.SMSObject;
import com.mashreq.transfercoreservice.notification.model.SMSResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "notification",
        url = "${app.services.notification}",
        configuration = FeignConfig.class)
public interface NotificationClient {

        @PostMapping(value = "/sms", consumes = "application/json")
        SMSResponse sendSMS(@RequestBody SMSObject smsObj);

        @PostMapping(value = "/common/notification/email", consumes = "application/json")
        EmailResponse sendEmail(@RequestBody EmailRequest emailRequest);

//        @PostMapping(value = "/common/message/sendNotification", consumes = "application/json")
//        NotificationResponse sendPushNotification(@RequestBody NotificationRequest notificationRequest, @RequestHeader("X-USSM-USER-CIF") String cif);
}
