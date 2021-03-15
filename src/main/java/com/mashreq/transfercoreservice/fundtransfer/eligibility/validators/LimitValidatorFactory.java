package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.CustomerClientType;
import com.mashreq.transfercoreservice.fundtransfer.limits.ILimitValidator;
import com.mashreq.transfercoreservice.fundtransfer.limits.LimitValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitValidatorFactory {

	private final LimitValidator retailLimitValidator;
	private final SMELimitValidator smeLimitValidator;
	
	public ILimitValidator getValidator(RequestMetaData metaData) {

		if(Objects.nonNull(metaData.getUserCacheKey())) {
			if(metaData.getUserCacheKey().equals(CustomerClientType.SME.name())) {
				return smeLimitValidator;
			}
		}
		
		return retailLimitValidator;
	}

}
