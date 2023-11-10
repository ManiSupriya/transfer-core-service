package com.mashreq.transfercoreservice.fundtransfer.limits;

import java.math.BigDecimal;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;

public interface ILimitValidator {

	LimitValidatorResponse validate(UserDTO userDTO, String beneficiaryType, BigDecimal paidAmount,
			RequestMetaData metaData, Long benId);

	void validateMin(UserDTO userDTO, String beneficiaryType, BigDecimal limitUsageAmount, RequestMetaData metadata);

	LimitValidatorResponse validateAvailableLimits(UserDTO userDTO, String serviceType, BigDecimal limitUsageAmount, RequestMetaData metaData, Long id);
}
