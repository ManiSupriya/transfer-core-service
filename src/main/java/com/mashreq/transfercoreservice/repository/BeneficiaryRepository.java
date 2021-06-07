package com.mashreq.transfercoreservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.model.Beneficiary;


@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
	
}
