package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SEGMENT;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.client.service.QuickRemitService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.CurrencyValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class QRAccountEligibilityService implements TransferEligibilityService {

	private final BeneficiaryService beneficiaryService;
	private final AccountService accountService;
	private final MaintenanceService maintenanceService;
	private final BeneficiaryValidator beneficiaryValidator;
	private final LimitValidatorFactory limitValidatorFactory;
	private final CurrencyValidatorFactory currencyValidatorFactory;
	private final QuickRemitService quickRemitService;
	private final AsyncUserEventPublisher userEventPublisher;
	 
	public void checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {

		log.info("Quick remit eligibility initiated");
		
		if(isSMESegment(metaData) && StringUtils.isNotBlank(request.getCardNo())) {
    		GenericExceptionHandler.handleError(INVALID_SEGMENT, INVALID_SEGMENT.getErrorMessage());
    	}
		
		final ValidationContext validationContext = new ValidationContext();
		
		final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), metaData);
        final String countryCodeISo = beneficiaryDto.getBeneficiaryCountryISO();
        
        List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", "AE", Boolean.TRUE);
        final Optional<CountryMasterDto> countryDto = countryList.stream()
                .filter(country -> country.getCode().equals(beneficiaryDto.getBankCountryISO()))
                .findAny();

        if (countryDto.isPresent()) {
        	validationContext.add("country", countryDto.get());
        }
        
        currencyValidatorFactory.getValidator(metaData).validate(request, metaData, validationContext);
        
        log.info("Initiating Quick Remit transfer to {}", countryCodeISo);
        
        validationContext.add("beneficiary-dto", beneficiaryDto);
        
        final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());

		responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

		final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore, request.getFromAccount());
		final CurrencyConversionDto currencyConversionDto = getAmountInSrcCurrency(request, beneficiaryDto, sourceAccountDetailsDTO);

		final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,
				currencyConversionDto.getAccountCurrencyAmount());
		
		limitValidatorFactory.getValidator(metaData).validate(userDTO, request.getServiceType(), limitUsageAmount, metaData);

		quickRemitService.exchange(request, countryDto, metaData);
		
	}
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.QRT;
	}

	private CurrencyConversionDto getAmountInSrcCurrency(FundTransferEligibiltyRequestDTO request, BeneficiaryDto beneficiaryDto,
			AccountDetailsDTO sourceAccountDetailsDTO) {

		final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
		currencyRequest.setAccountNumber(sourceAccountDetailsDTO.getNumber());
		currencyRequest.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
		currencyRequest.setTransactionCurrency(beneficiaryDto.getBeneficiaryCurrency());
		currencyRequest.setProductCode(request.getProductCode());
		currencyRequest.setTransactionAmount(request.getAmount());

		return maintenanceService.convertBetweenCurrencies(currencyRequest);
	}

	private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		return "AED".equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
				? transferAmountInSrcCurrency
						: convertAmountInLocalCurrency(dealNumber, sourceAccountDetailsDTO, transferAmountInSrcCurrency);
	}

	private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
		currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
		currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
		currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
		currencyConversionRequestDto.setDealNumber(dealNumber);
		currencyConversionRequestDto.setTransactionCurrency("AED");

		CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
		return currencyConversionDto.getTransactionAmount();
	}




}
