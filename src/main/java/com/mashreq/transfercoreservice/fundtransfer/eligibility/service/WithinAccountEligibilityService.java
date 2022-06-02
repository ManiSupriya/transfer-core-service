package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.mashreq.transfercoreservice.errors.ExceptionUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.BeneficiaryService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.BeneficiaryValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithinAccountEligibilityService implements TransferEligibilityService{

	private final BeneficiaryValidator beneficiaryValidator;
	private final AccountService accountService;
	private final BeneficiaryService beneficiaryService;
	private final LimitValidatorFactory limitValidatorFactory;
	private final MaintenanceService maintenanceService;
	private final AsyncUserEventPublisher auditEventPublisher;

	@Value("${app.local.currency}")
	private String localCurrency;

	@Override
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {
		log.info("WithinAccountEligibility validation started");
		final ValidationContext validationContext = new ValidationContext();
		validationContext.add("validate-from-account", Boolean.TRUE);

		final AccountDetailsDTO accountDetails = accountService.getAccountDetailsFromCache(request.getFromAccount(), metaData);
		validationContext.add("from-account", accountDetails);

		BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(metaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), "V2", metaData);
		validationContext.add("beneficiary-dto", beneficiaryDto);
		responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

		final BigDecimal transferAmountInSrcCurrency = isCurrencySame(request) ? request.getAmount()
				: getAmountInSrcCurrency(request, beneficiaryDto, accountDetails);

		validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);

		// Limit Validation
		Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId()) ? Long.parseLong(request.getBeneficiaryId())
				: null;

		limitValidatorFactory.getValidator(metaData).validate(
				userDTO, 
				request.getServiceType(), 
				getLimitUsageAmount(request.getDealNumber(), accountDetails,transferAmountInSrcCurrency),
				metaData, 
				bendId);
		log.info("WithinAccountEligibility validation successfully finished");
		return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).build();
	}

	@Override
	public ServiceType getServiceType() {
		return ServiceType.WAMA;
	}

	private boolean isCurrencySame(FundTransferEligibiltyRequestDTO request) {
		return request.getCurrency().equalsIgnoreCase(request.getTxnCurrency());
	}

	private BigDecimal getAmountInSrcCurrency(FundTransferEligibiltyRequestDTO request, BeneficiaryDto beneficiaryDto,
			AccountDetailsDTO fromAccount) {
		final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
		currencyRequest.setAccountNumber(fromAccount.getNumber());
		currencyRequest.setAccountCurrency(fromAccount.getCurrency());
		currencyRequest.setTransactionCurrency(request.getTxnCurrency());
		currencyRequest.setDealNumber(request.getDealNumber());
		currencyRequest.setTransactionAmount(request.getAmount());
		return maintenanceService.convertBetweenCurrencies(currencyRequest).getAccountCurrencyAmount();
	}


	private BigDecimal getLimitUsageAmount(final String dealNumber, final AccountDetailsDTO accountDetails,
		final BigDecimal transferAmountInSrcCurrency) {
		return localCurrency.equalsIgnoreCase(accountDetails.getCurrency())
				? transferAmountInSrcCurrency
						: convertAmountInLocalCurrency(dealNumber, accountDetails, transferAmountInSrcCurrency);
	}

	private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
		currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
		currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
		currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
		currencyConversionRequestDto.setTransactionCurrency(localCurrency);

		CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
		return currencyConversionDto.getTransactionAmount();
	}
}
