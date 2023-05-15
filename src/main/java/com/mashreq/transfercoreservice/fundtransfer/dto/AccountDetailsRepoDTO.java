package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "npss_account_details")
public class AccountDetailsRepoDTO implements Serializable {
    private static final long serialVersionUID = -653788341775043008L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column
    private String accountName;
    @Column
    private String accountType;
    @Column
    private String currency;
    @Column
    private String accountNumber;

    @Column
    private String ibanNumber;

    @Column
    private String status;

    @Column
    private String schemeType;

    @Column
    private String segment;

    @Column
    private String branchCode;

    @Column
    private String type;

    @Column(name = "cif_id")
    private String cifId;

    @Column(name = "enrollment_id")
    private String enrollmentId;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now(ZoneId.of("GMT+04"));
}
