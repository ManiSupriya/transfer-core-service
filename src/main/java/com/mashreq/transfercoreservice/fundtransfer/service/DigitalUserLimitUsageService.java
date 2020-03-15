package com.mashreq.transfercoreservice.fundtransfer.service;


import com.mashreq.transfercoreservice.fundtransfer.dto.DigitalUserLimitUsageDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserLimitUsageDTO;

import java.util.Optional;

public interface DigitalUserLimitUsageService {

    /**
     *
     */
    public void insert(DigitalUserLimitUsageDTO digitalUserLimitUsageDTO);

    /**
     * Get consumed MONTHLY limit by cif and beneficiary type
     */
    public Optional<UserLimitUsageDTO> getMonthlyLimitUsageByBillerTypeAndCif(String beneficiaryTypeCode, String cif);

    /**
     * Get consumed DAILY limit by cif and beneficiary type
     */
    public Optional<UserLimitUsageDTO> getDailyLimitUsageByBillerTypeAndCif(String beneficiaryTypeCode, String cif);

}
