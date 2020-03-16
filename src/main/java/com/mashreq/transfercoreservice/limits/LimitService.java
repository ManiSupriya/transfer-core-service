package com.mashreq.transfercoreservice.limits;

import java.util.Optional;

/**
 *
 */
public interface LimitService {

    /**
     * Get limit for a beneficiary type and segment and country
     */
    public Optional<LimitDTO> getDefaultLPByBeneficiaryTypeAndSegmentAndCountry(String beneficiaryType, Long segmentId, Long countryId);

    /**
     * Get (optional) limit for a user for a beneficiary type
     * Check if the limitNature is P or T with date range
     */
    public Optional<LimitDTO> getUserLPByUserId();

    /**
     * Get (optional) limit for a customer for a beneficiary type
     * Check if the limitNature is P or T with date range
     */
    public Optional<LimitDTO> getCustomerLPByCif();
}
