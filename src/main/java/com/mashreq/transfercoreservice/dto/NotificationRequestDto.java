package com.mashreq.transfercoreservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by KrishnaKo on 24/11/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDto implements Serializable {
    private String limitVersionUuid;
    private BigDecimal amount;
    private String notificationType;
    /*Added For NPSS check and remove above as required*/
    private String customerName;
    private String contactName;
    private String referenceNumber;
    private String sentTo;
    private String date;
    private String time;
    private String reasonForFailure;
    private String fromAccount;
    private String receiverName;
    private List<RtpNotification> rtpNotificationList;
    private String emailProxy;
    private String proxy;
}
