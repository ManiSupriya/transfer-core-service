package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.LimitValidatorResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.limits.ILimitValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMELimitValidator implements ILimitValidator{

	@Override
	public LimitValidatorResponse validate(UserDTO userDTO, String beneficiaryType, BigDecimal paidAmount,
			RequestMetaData metaData, Long benId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validateMin(UserDTO userDTO, String beneficiaryType, BigDecimal limitUsageAmount,
			RequestMetaData metadata) {
		// TODO Auto-generated method stub
		
	}

}