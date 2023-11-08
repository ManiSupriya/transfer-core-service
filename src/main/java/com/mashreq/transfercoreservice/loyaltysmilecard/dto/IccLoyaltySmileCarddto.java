package com.mashreq.transfercoreservice.loyaltysmilecard.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "icc_loyalty_ms")
@Data
@NoArgsConstructor
public class IccLoyaltySmileCarddto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String cif;
	@Column(nullable = false)
	private String userSessionId;
	@Column(nullable = false)
	private String sessionId;
	@Column(nullable = false)
	private LocalDateTime createdTime;
	@Column(nullable = false)
	private LocalDateTime updatedTime;
	
}
