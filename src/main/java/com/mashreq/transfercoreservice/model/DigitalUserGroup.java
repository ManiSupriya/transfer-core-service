package com.mashreq.transfercoreservice.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@EqualsAndHashCode(of = "name", callSuper = false)
@Entity
@Table(name = "digital_user_group_ms")
@Getter
@Setter
public class DigitalUserGroup extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    @OneToOne(fetch = FetchType.LAZY)
    private DigitalUserType digitalUserType;
    @OneToOne(fetch = FetchType.EAGER)
    private Segment segment;
    @OneToOne(fetch = FetchType.EAGER)
    private Country country;
}