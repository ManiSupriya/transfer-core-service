package com.mashreq.transfercoreservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;


/**
 * The persistent class for the digital_user_limit_usage database table.
 * 
 */
@Entity
@Table(name="digital_user_limit_usage")
@ToString
@Getter
@Setter
public class DigitalUserLimitUsage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String beneficiaryTypeCode;
	@Column(nullable = false)
	private String channel;
	@Column(nullable = false)
	private String cif;
	@Column(nullable = false)
	private String createdBy;

	@CreatedDate
	@Column(name = "created_date", updatable = false)
	private Instant createdDate = Instant.now();

	@Column(nullable = false)
	private long digitalUserId;

	@Column(nullable = false)
	private BigDecimal paidAmount;

	@Column(nullable = false)
	private String versionUuid;
}