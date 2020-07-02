package com.mashreq.transfercoreservice.event.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.Instant;


/**
 * The persistent class for the user_event_audit database table.
 */
@Entity
@Table(name = "user_event_audit")
@Getter
@Setter
public class UserEventAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventName;
    private String username;
    private String status;
    private String channel;
    private String correlationId;
    private String sessionCacheKey;
    private String actionKey;
    private String mwSrcMsgId;
    private String eventCategory;
    private String errorCode;
    private String errorDetails;
    private String cif;
    private String errorDescription;
    private String region;
    private String clientIp;
    private String remarks;


    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_on", updatable = false)
    private Instant createdOn = Instant.now();

}
