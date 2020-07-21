package com.mashreq.transfercoreservice.fundtransfer.limits;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.mashreq.transfercoreservice.cardlesscash.dto.response.LimitValidatorResponse;

@Repository
public interface DigitalUserLimitUsageRepository extends JpaRepository<DigitalUserLimitUsage, Long> {
	
	@Procedure(procedureName = "USP_VALIDATE_LIMIT")
	LimitValidatorResponse checkLimit(String cif, String trxType, String countryCode, String segment, int benId,
			BigDecimal amount);

}
