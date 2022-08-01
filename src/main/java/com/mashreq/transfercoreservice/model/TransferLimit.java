package com.mashreq.transfercoreservice.model;

import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfer_limit")
@ToString
@Getter
@Setter
public class TransferLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long beneficiaryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 2)
    private FTOrderType orderType;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate = Instant.now();
}