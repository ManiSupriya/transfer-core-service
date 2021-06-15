package com.mashreq.transfercoreservice.paylater.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.mashreq.transfercoreservice.paylater.utils.DateTimeUtil;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class DomainEntity extends BaseEntity {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6114615975792779706L;
	@Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn = DateTimeUtil.getCurrentDateTimeZone();
    @Column(name = "created_by")
	private String createdBy;
}
