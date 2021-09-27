package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.mashreq.transfercoreservice.errors.ExceptionUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import org.apache.commons.lang3.StringUtils;
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

	public static final String LOCAL_CURRENCY = "AED";

	private final BeneficiaryValidator beneficiaryValidator;
	private final AccountService accountService;
	private final BeneficiaryService beneficiaryService;
	private final LimitValidatorFactory limitValidatorFactory;
	private final MaintenanceService maintenanceService;
	private final AsyncUserEventPublisher auditEventPublisher;

	@Override
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request,
			UserDTO userDTO) {
		log.info("WithinAccountEligibility validation started");
		final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());
		final ValidationContext validationContext = new ValidationContext();
		validationContext.add("account-details", accountsFromCore);
		validationContext.add("validate-from-account", Boolean.TRUE);

		Optional<AccountDetailsDTO> fromAccountOpt = accountsFromCore.stream()
				.filter(x -> request.getFromAccount().equals(x.getNumber()))
				.findFirst();

		validationContext.add("from-account", fromAccountOpt.orElseThrow(() -> ExceptionUtils.genericException(TransferErrorCode.ACCOUNT_NOT_FOUND)));

		BeneficiaryDto beneficiaryDto = beneficiaryService.getByIdWithoutValidation(metaData.getPrimaryCif(), Long.valueOf(request.getBeneficiaryId()), "V2", metaData);
		validationContext.add("beneficiary-dto", beneficiaryDto);
		responseHandler(beneficiaryValidator.validate(request, metaData, validationContext));

		final BigDecimal transferAmountInSrcCurrency = isCurrencySame(request) ? request.getAmount()
				: getAmountInSrcCurrency(request, beneficiaryDto, fromAccountOpt);

		validationContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);

		// Limit Validation
		Long bendId = StringUtils.isNotBlank(request.getBeneficiaryId()) ? Long.parseLong(request.getBeneficiaryId())
				: null;

		limitValidatorFactory.getValidator(metaData).validate(
				userDTO, 
				request.getServiceType(), 
				getLimitUsageAmount(request.getDealNumber(), fromAccountOpt,transferAmountInSrcCurrency), 
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
			Optional<AccountDetailsDTO> fromAccountOpt) {
		BigDecimal amtToBePaidInSrcCurrency = null;
		if(fromAccountOpt.isPresent()) {
			final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
			currencyRequest.setAccountNumber(fromAccountOpt.get().getNumber());
			currencyRequest.setAccountCurrency(fromAccountOpt.get().getCurrency());
			currencyRequest.setTransactionCurrency(request.getTxnCurrency());
			currencyRequest.setDealNumber(request.getDealNumber());
			currencyRequest.setTransactionAmount(request.getAmount());
			CurrencyConversionDto conversionResultInSourceAcctCurrency = maintenanceService.convertBetweenCurrencies(currencyRequest);
			amtToBePaidInSrcCurrency = conversionResultInSourceAcctCurrency.getAccountCurrencyAmount();
		}
		return amtToBePaidInSrcCurrency;
	}


	private BigDecimal getLimitUsageAmount(final String dealNumber, final Optional<AccountDetailsDTO> fromAccountOpt,
			final BigDecimal transferAmountInSrcCurrency) {
		if(fromAccountOpt.isPresent()) {
			return LOCAL_CURRENCY.equalsIgnoreCase(fromAccountOpt.get().getCurrency())
					? transferAmountInSrcCurrency
							: convertAmountInLocalCurrency(dealNumber, fromAccountOpt.get(), transferAmountInSrcCurrency);
		}
		return null;
	}

	private BigDecimal convertAmountInLocalCurrency(final String dealNumber, final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
		currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
		currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
		currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
		currencyConversionRequestDto.setTransactionCurrency(LOCAL_CURRENCY);

		CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
		return currencyConversionDto.getTransactionAmount();
	}
}
