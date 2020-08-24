package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

@Data
public class SMSObject {
    String message;
    String mobileNumber;
    String priority;
    String serviceId;
}
