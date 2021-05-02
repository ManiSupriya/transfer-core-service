package com.mashreq.transfercoreservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the digital_user database table.
 */
@EqualsAndHashCode(of = "mobile", callSuper = false)
@Entity
@Table(name = "digital_user",
        indexes = {@Index(columnList = "primary_mobile", name = "digital_user_mobile_hidx"),
                @Index(columnList = "username", name = "digital_user_username_hidx")})
@Getter
@Setter
public class DigitalUser extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String cif;
    private String firstName;
    private String lastName;
    private boolean primaryFlag;
    private String country;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false, name = "primary_mobile")
    private String mobile;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "digital_user_group_id")
    private DigitalUserGroup digitalUserGroup;
    String deviceidRegisteredForPushnotify;
    String deviceInfo;

    public String fullName() {
        return firstName + " " + lastName;
    }


}