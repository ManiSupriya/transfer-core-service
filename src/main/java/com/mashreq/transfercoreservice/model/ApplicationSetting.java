package com.mashreq.transfercoreservice.model;

import lombok.Data;

import jakarta.persistence.*;

/**
 * @author shahbazkh
 * @date 4/2/20
 */

@Data
@Entity
@Table(name = "application_setting_ms")
public class ApplicationSetting extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String settingKey;

    @Column
    private String settingValue;

    @Column
    private boolean deleted;

    @Column(length = 10)
    private String channelName;

    @Column(length = 3)
    private String region;
}
