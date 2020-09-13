package com.mashreq.transfercoreservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.loyalty.dto.IccLoyaltydto;
@Repository
public interface IccLoyaltyRepository extends JpaRepository<IccLoyaltydto, Long> {

}
