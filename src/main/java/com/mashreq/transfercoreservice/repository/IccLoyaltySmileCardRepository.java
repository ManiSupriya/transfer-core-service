package com.mashreq.transfercoreservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.loyaltysmilecard.dto.IccLoyaltySmileCarddto;
@Repository
public interface IccLoyaltySmileCardRepository extends JpaRepository<IccLoyaltySmileCarddto, Long> {
	Optional<IccLoyaltySmileCarddto> findBySessionIdEquals(String sessionId);
}
