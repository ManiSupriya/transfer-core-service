package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="npss_enrollment")
@Data
@NoArgsConstructor
public class NpssEnrolmentRepoDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cif_id;
    @Column(nullable = false)
    private String enrollment_status;
    @Column(nullable = false)
    private LocalDateTime accepted_date;
}
