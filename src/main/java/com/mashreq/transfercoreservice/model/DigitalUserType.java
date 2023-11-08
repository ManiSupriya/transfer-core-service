package com.mashreq.transfercoreservice.model;//package com.mashreq.mobcustomerservice.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@EqualsAndHashCode(of = "name", callSuper = false)
@Entity
@Table(name = "digital_user_type_ms")
@Getter
@Setter
public class DigitalUserType extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
}
