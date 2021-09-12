package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.CustomerClientType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyValidatorFactory {

	private final CurrencyValidator retailCurrencyValidator;
	private final SMECurrencyValidator smeCurrencyValidator;
	
	public ICurrencyValidator getValidator(RequestMetaData metaData) {

		if(Objects.nonNull(metaData.getUserType())) {
			if(metaData.getUserType().equals(CustomerClientType.SME.name())) {
				return smeCurrencyValidator;
			}
		}
		
		return retailCurrencyValidator;
	}

}
