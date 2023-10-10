package com.mashreq.transfercoreservice.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter @Setter
@Entity
@Table(name = "qr_deals_details")
public class QRDealsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cif")
    private String cif;

    @Column(name = "eligible_countries")
    private String eligibleCountries;

    @Column(name = "total_limit_amount")
    private BigDecimal totalLimitAmount;

    @Column(name = "utilized_amount")
    private BigDecimal utilizedLimitAmount;

    @Column(name = "eligibility_start_date")
    private Date eligibilityStartDate;

    @Column(name = "eligibility_end_date")
    private Date eligibilityEndDate;
}
