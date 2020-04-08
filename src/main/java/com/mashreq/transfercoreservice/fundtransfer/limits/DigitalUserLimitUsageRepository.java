package com.mashreq.transfercoreservice.limits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DigitalUserLimitUsageRepository extends JpaRepository<DigitalUserLimitUsage, Long> {
}
