package com.mashreq.transfercoreservice.limits;

import com.mashreq.transfercoreservice.limits.LimitPackageDefault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitPackageDefaultRepository extends JpaRepository<LimitPackageDefault, Long> {
    LimitPackageDefault findByBeneficiaryTypeCodeAndSegmentIdAndCountryId(String beneficiaryType, Long segmentId, Long countryId);
}
