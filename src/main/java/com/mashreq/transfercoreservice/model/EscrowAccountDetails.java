package com.mashreq.transfercoreservice.model;

import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer;
import lombok.*;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@Table(name = "escrow_account_details")
@AllArgsConstructor
@NoArgsConstructor
public class EscrowAccountDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cif",length = 20,nullable = false)
    private String cif;
    
    @Column(name = "project_no",length = 30,nullable = true)
    private String projectNo;
    
    @Column(name = "account_no",length = 30,nullable = false)
    private String accountNo;
    
    @Column(name = "account_type",length = 20,nullable = true)
    private String accountType;
    
    @Column(name = "project_name",length = 255,nullable = true)
    private String projectName;
    
    @Column(name = "developer_name",length = 255,nullable = true)
    private String developerName;
    
    @Column(name = "retention_account_no",length = 20,nullable = true)
    private String retentionAccountNo;

	@Column(name = "created_by", length = 50, updatable = false)
	private String createdBy;

	@Column(name = "created_date", updatable = false)
	private Instant createdDate = Instant.now();

	@Column(name = "updated_by", length = 50)
	private String updatedBy;

	@Column(name = "updated_date")
	private Instant updatedDate = Instant.now();
}
