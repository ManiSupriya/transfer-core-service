package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_SEGMENT;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CountryMasterDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.QRExchangeResponse;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.client.service.QuickRemitService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
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
	 
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {

		log.info("Quick remit eligibility initiated");
		
		Optional<CountryMasterDto> countryDto;
		if (isSMESegment(metaData) && StringUtils.isNotBlank(request.getCardNo())) {
			return EligibilityResponse.builder().status(FundsTransferEligibility.NOT_ELIGIBLE)
					.errorCode(INVALID_SEGMENT.getCustomErrorCode()).errorMessage(INVALID_SEGMENT.getErrorMessage())
					.build();
		}

		final ValidationContext validationContext = new ValidationContext();

		final BeneficiaryDto beneficiaryDto = beneficiaryService.getById(metaData.getPrimaryCif(),
				Long.valueOf(request.getBeneficiaryId()), metaData);
		final String countryCodeISo = beneficiaryDto.getBeneficiaryCountryISO();

		List<CountryMasterDto> countryList = maintenanceService.getAllCountries("MOB", "AE", Boolean.TRUE);
		countryDto = countryList.stream()
				.filter(country -> country.getCode().equals(beneficiaryDto.getBankCountryISO())).findAny();

		if (countryDto.isPresent()) {
			validationContext.add("country", countryDto.get());
		}

		currencyValidatorFactory.getValidator(metaData).validate(request, metaData, validationContext);

		log.info("Initiating Quick Remit transfer to {}", countryCodeISo);

		validationContext.add("beneficiary-dto", beneficiaryDto);

		final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());

		responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));
		// corrected logic to use right account currency amount
		final AccountDetailsDTO sourceAccountDetailsDTO = getAccountDetailsBasedOnAccountNumber(accountsFromCore,
				request.getFromAccount());
		QRExchangeResponse response = quickRemitService.exchange(request, countryDto, metaData);
		if (!response.isAllowQR()) {
			return EligibilityResponse.builder().status(FundsTransferEligibility.NOT_ELIGIBLE).data(response).build();
		}
		final BigDecimal limitUsageAmount = getLimitUsageAmount(request.getDealNumber(), sourceAccountDetailsDTO,
				new BigDecimal(response.getAccountCurrencyAmount()));
		limitValidatorFactory.getValidator(metaData).validate(userDTO, request.getServiceType(), limitUsageAmount,
				metaData);
		return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).data(response).build();
	}

	@Override
	public ServiceType getServiceType() {
		return ServiceType.QRT;
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
