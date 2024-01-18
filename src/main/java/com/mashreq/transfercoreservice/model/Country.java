package com.mashreq.transfercoreservice.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;


/**
 * The persistent class for the country database table.
 */
@EqualsAndHashCode(of = "name", callSuper = false)
@ToString
@Entity
@Table(name = "country_ms",
        indexes = {@Index(columnList = "name", name = "country_name_hidx")})
@Getter
@Setter
public class Country extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(length = 4, name = "isocode2", unique = true)
    private String isoCode2;
    @Column(length = 4, name = "isocode3", unique = true)
    private String isoCode3;
    @Column(length = 100)
    private String localCurrency;
}