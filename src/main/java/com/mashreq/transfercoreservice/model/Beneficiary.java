package com.mashreq.transfercoreservice.model;

import java.time.Instant;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Where(clause = "deleted = 0")
@Table(name = "beneficiary")
@Getter
@Setter
@ToString
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(nullable = false)
    @NotBlank
    private String cif;
    private String secondaryCif;
    @NotBlank
    @Column(nullable = false)
    private String accountNumber;
    @NotBlank
    @Column(nullable = false)
    private String nickname;

    private String fullName;

    
    //Final name is only for international and for exchange house mostly
    private String finalName;
    private String swiftCode;
    private String routingCode;
    private String bankName;
    private String bankCountry;
    private String bankCountryISO;
    private String bankCity;
    private String bankBranchName;
    private String bankRoutingCode;
    private String bankState;
    private String bankCode;
    private String bankSearchType;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String beneficiaryCity;
    private String beneficiaryState;
    private String beneficiaryPostalCode;
    private String beneficiaryCountry;
    private String mobileNumber;
    private String emailAddress;
    private String documentNumber;
    private String documentExpiry;
    private String transactionProcessor;
    private String source;
    
    private String bankBranchCode;
    
    private String preferredCcy;

    // status
    private Instant activeAfter;
    @Column(nullable = false)
    private Boolean deleted = false;
    @Column(nullable = false)
    private Boolean favorite = false;
    @Column(nullable = false)
    private Boolean myAccInOtherBank = false;

    // audit
    @CreatedBy
    @Column(name = "created_by", length = 50)
    private String createdBy;


    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    private String maskedCardNumber;

    @Column(name = "created_date", length = 50)
    private Instant createdDate;

    @Column(name = "last_modified_date", length = 50)
    private Instant lastModifiedDate;
    
    private Long beneficiaryUniqueRefNo;
    
    private Instant coolingStartDate;

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }
}
