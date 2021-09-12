package com.mashreq.transfercoreservice.paylater.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5413295972082551656L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String getIdAsString() {
        return String.valueOf(getId());
    }
}
 