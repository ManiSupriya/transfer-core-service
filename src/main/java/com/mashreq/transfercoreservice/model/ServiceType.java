package com.mashreq.transfercoreservice.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_type_ms")
@Getter @Setter
public class ServiceType {

	@Id
	@Column(name = "code", length = 20)
	private String code;
	@Column(length = 100)
	private String name;
	private String serviceGroup;
	// configs
	private Long coolingPeriodMin = 0L;
	private String minAmount;
	private String maxAmount;
	private String multipleOf;
	private Boolean fractialPayment;
	private Boolean partialPayment;
	private Boolean overPayment;
	private Boolean availableBalance;
	private Boolean dueAmount;
}