package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Data
@Builder
@Table(name="npss_enrollment")
public class NpssEnrolmentRepoDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String cif_id;
    @Column
    private String enrollment_status;
    @Column
    private Instant accepted_date;
}
