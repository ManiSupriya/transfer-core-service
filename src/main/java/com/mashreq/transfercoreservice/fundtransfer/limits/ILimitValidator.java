package com.mashreq.transfercoreservice.fundtransfer.limits;

import java.math.BigDecimal;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.mobcommon.dto.LimitValidatorResultsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;

public interface ILimitValidator {

	LimitValidatorResultsDto validate(UserDTO userDTO, String beneficiaryType, BigDecimal paidAmount,
			RequestMetaData metaData);

	LimitValidatorResponse validateWithProc(UserDTO userDTO, String beneficiaryType, BigDecimal paidAmount,
			RequestMetaData metaData, Long benId);

	void validateMin(UserDTO userDTO, String beneficiaryType, BigDecimal limitUsageAmount, RequestMetaData metadata);

}
