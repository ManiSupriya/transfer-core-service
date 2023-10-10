package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class NotificationRequest {

    @NotBlank
    private String message;

    @NotBlank
    private String channelId;

    @NotNull
    private DeviceInfo deviceInfo;

}
