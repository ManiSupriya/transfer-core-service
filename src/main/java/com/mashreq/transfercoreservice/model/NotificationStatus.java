package com.mashreq.transfercoreservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Notification_Status")
@Getter
@Setter
public class NotificationStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String notificationName;
    private String notificationType;
    private String status;
    private Date createdDate;
    private String txnRefNo;
}