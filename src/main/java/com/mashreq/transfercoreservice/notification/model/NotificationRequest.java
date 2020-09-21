package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NotificationRequest {

    @NotBlank
    private String message;

    @NotBlank
    private String channelId;

    @NotNull
    private DeviceInfo deviceInfo;

}
