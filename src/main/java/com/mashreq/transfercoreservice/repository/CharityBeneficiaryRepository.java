package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.CharityBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharityBeneficiaryRepository extends JpaRepository<CharityBeneficiary, Long> {
}
