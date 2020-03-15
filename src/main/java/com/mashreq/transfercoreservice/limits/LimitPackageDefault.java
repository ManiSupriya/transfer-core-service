package com.mashreq.transfercoreservice.limits;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the limit_package_default_ms database table.
 * 
 */
@Entity
@Table(name="limit_package_default_ms")
@ToString
@Getter
@Setter
public class LimitPackageDefault  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String beneficiaryTypeCode;

	@Column(nullable = false)
	private long countryId;

	@Column(nullable = false)
	private long segmentId;

	@Column(name="max_amount_daily")
	private BigDecimal maxAmountDaily;

	@Column(name="max_amount_monthly")
	private BigDecimal maxAmountMonthly;

	@Column(name="max_count_daily")
	private int maxCountDaily;

	@Column(name="max_count_monthly")
	private int maxCountMonthly;

	@Column(name="max_trx_amount")
	private BigDecimal maxTrxAmount;

	@Column(name="version_uuid")
	private String versionUuid;
}