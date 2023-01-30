package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="npss_enrollment")
public class NpssEnrolmentRepoDTO implements Serializable {
    private static final long serialVersionUID = -653788445775043008L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column
    private String cif_id;
    @Column
    private String enrollment_status;
    @Column
    private String created_date;
    @Column
    private Instant accepted_date;
    @Column(nullable = false)
    private boolean is_default_account_updated = Boolean.FALSE;

    @OneToMany(cascade = CascadeType.ALL ,fetch = FetchType.LAZY)
    @JoinColumn(name = "cif_id")
    private List<AccountDetailsRepoDTO> accountDetails;
}
