package com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.TwoFactorAuthRequiredCheckResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.limits.DigitalUserLimitUsageRepository;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.config.TwoFactorAuthRequiredValidationConfig;
import com.mashreq.transfercoreservice.twofactorauthrequiredvalidation.service.TwoFactorAuthRequiredCheckService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthRequiredCheckServiceImpl implements TwoFactorAuthRequiredCheckService {
	private final TwoFactorAuthRequiredValidationConfig config;
	private final MaintenanceService maintenanceService;
	private final BeneficiaryService beneficiaryService;
	private final DigitalUserLimitUsageRepository digitalUserLimitUsageRepository;
	@Value("${app.local.currency}")
	private String localCurrency;
	
	@Override
	public TwoFactorAuthRequiredCheckResponseDto checkIfTwoFactorAuthenticationRequired(RequestMetaData metaData,
			TwoFactorAuthRequiredCheckRequestDto requestDto) {
		TwoFactorAuthRequiredCheckResponseDto dto = new TwoFactorAuthRequiredCheckResponseDto();
		try {
			if(!config.getTwofactorAuthRelaxed() || beneficiaryService.isRecentlyUpdated(requestDto,metaData,config)) {
				return dto;
			}
			/**account number belongs to cif validation needs to be done.*/
			BigDecimal localCurrencyAmount = maintenanceService.convertToLocalCurrency(requestDto,metaData,localCurrency);
			if(validateAmount(localCurrencyAmount)) {
				return dto;
			}
			validateTransactionCount(requestDto,dto);
		} catch (Exception e) {
			log.error("Error occurred while validating whether otp validation is required with {} ",ExceptionUtils.getStackTrace(e));
			log.info("defaulting to otp required");
		}
		return dto;
	}

	private boolean validateAmount(BigDecimal localCurrencyAmount) {
		return localCurrencyAmount.compareTo(new BigDecimal(config.getMaxAmountAllowed())) > 0;
	}

	private void validateTransactionCount(TwoFactorAuthRequiredCheckRequestDto requestDto,
			TwoFactorAuthRequiredCheckResponseDto dto) {
		Long beneficiaryId = Long.valueOf(requestDto.getBeneficiaryId());
		Instant to = Instant.now(),from = Instant.now().minus(config.getDurationInHours(), ChronoUnit.HOURS);
		Long trxCount = digitalUserLimitUsageRepository.findCountForBeneficiaryIdBetweendates(beneficiaryId,from, to);
		dto.setTwoFactorAuthRequired(trxCount >= config.getNoOfTransactionsAllowed());
	}

}
