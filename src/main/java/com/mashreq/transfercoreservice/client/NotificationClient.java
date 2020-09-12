package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.config.feign.FeignConfig;
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
}
