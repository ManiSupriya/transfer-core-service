package com.mashreq.transfercoreservice.fundtransfer.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonService;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealConversionRateRequestDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealConversionRateResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealValidator implements Validator {

	private final MobCommonService mobCommonService;

	@Override
	public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata,
			ValidationContext context) {
			if (StringUtils.isNotBlank(request.getDealNumber())) {
				DealConversionRateRequestDto dealConversionRateRequestDto = new DealConversionRateRequestDto();
			dealConversionRateRequestDto.setAccountNumber(request.getFromAccount());
			dealConversionRateRequestDto.setAccountCurrency(request.getCurrency());
			dealConversionRateRequestDto.setTransactionType(request.getServiceType());
			dealConversionRateRequestDto.setTransactionAmount(request.getAmount());
			dealConversionRateRequestDto.setTransactionCurrency(request.getTxnCurrency());
			dealConversionRateRequestDto.setDealNumber(request.getDealNumber());
			DealConversionRateResponseDto dealConversionRateResponseDto = mobCommonService.getConvertBetweenCurrenciesWithDeal(dealConversionRateRequestDto);
			log.info("Deal Validation is success {}", dealConversionRateResponseDto);
			request.setDealRate(dealConversionRateResponseDto.getDealEnquiry().getDealRate());
			}	
		
		return ValidationResult.builder().success(true).build();
	}
}
