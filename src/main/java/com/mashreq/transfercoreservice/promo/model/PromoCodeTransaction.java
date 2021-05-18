package com.mashreq.transfercoreservice.promo.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;

@Table(name = "promo_code_transaction")
@Entity
@Data
@Builder
public class PromoCodeTransaction {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private String code;
	private String cif;
	private String sndrAcNum;
	private Date createdOn;
	private String orderStatus;
	private BigDecimal transAmnt;
}
