package com.mashreq.transfercoreservice.promo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.promo.model.PromoCodeTransaction;

@Repository
public interface PromoCodeTransactionRepository extends JpaRepository<PromoCodeTransaction, Long> {
	
	
}