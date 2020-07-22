package com.mashreq.transfercoreservice.fundtransfer.limits;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.cardlesscash.dto.response.LimitValidatorResponse;

@Repository
public interface DigitalUserLimitUsageRepository extends JpaRepository<DigitalUserLimitUsage, Long> {
	
	@Query(value = "CALL USP_VALIDATE_LIMIT(:cifId, :trxType, :countryCode, :segment, :beneId, :payAmountInLocalCurrency);", nativeQuery = true)
	LimitValidatorResponse checkLimit(@Param("cifId") String cif,
            @Param("trxType") String trxType,
            @Param("countryCode") String countryCode, @Param("segment") String segment,
            @Param("beneId") int benId, @Param("payAmountInLocalCurrency") BigDecimal amount);

}
