package com.mashreq.transfercoreservice.fundtransfer.limits;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DigitalUserLimitUsageRepository extends JpaRepository<DigitalUserLimitUsage, Long> {

	@Query("select count(id) from DigitalUserLimitUsage usg where usg.beneficiaryId=:beneficiaryId and usg.createdDate between :fromDate and :toDate")
	Long findCountForBeneficiaryIdBetweendates(Long beneficiaryId, Instant fromDate, Instant toDate);


}
