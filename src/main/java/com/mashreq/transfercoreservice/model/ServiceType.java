package com.mashreq.transfercoreservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "service_type_ms")
@Getter @Setter
public class ServiceType {

	@Id
	@Column(name = "code", length = 20)
	private String code;

	@Column(length = 100)
	private String name;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "country_id")
	private Country country;

	private Integer suggestedPosition;

	// configs
	private Long coolingPeriodMin = 0L;
	private String minAmount;
	private String maxAmount;
	private Boolean fractialPayment;
	private Boolean partialPayment;
	private Boolean overPayment;
	private Boolean advancePayment;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "service_type_payment_ranges_ms",
			joinColumns = @JoinColumn(columnDefinition = "DECIMAL(7,2)"))
	private List<BigDecimal> paymentRange;

}