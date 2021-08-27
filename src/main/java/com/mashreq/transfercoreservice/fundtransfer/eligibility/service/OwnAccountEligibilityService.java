package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.service.AccountService;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.enums.FundsTransferEligibility;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.AccountBelongsToCifValidator;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.validators.LimitValidatorFactory;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnAccountEligibilityService implements TransferEligibilityService{

	public static final String OWN_ACCOUNT_TRANSACTION_CODE = "096";
	public static final String LOCAL_CURRENCY = "AED";
	public static final String OWN_ACCOUNT = "Own Account";

	private final AccountBelongsToCifValidator accountBelongsToCifValidator;
	private final LimitValidatorFactory limitValidatorFactory;
	private final AccountService accountService;
	private final MaintenanceService maintenanceService;

	@Override
	public EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request, UserDTO userDTO) {
		log.info("OwnAccountEligibility validation started");
		final List<AccountDetailsDTO> accountsFromCore = accountService.getAccountsFromCore(metaData.getPrimaryCif());

		final ValidationContext validateAccountContext = new ValidationContext();
		validateAccountContext.add("account-details", accountsFromCore);
		validateAccountContext.add("validate-to-account", Boolean.TRUE);
		validateAccountContext.add("validate-from-account", Boolean.TRUE);

		responseHandler(accountBelongsToCifValidator.validate(request, metaData, validateAccountContext));

		final AccountDetailsDTO toAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore,
				request.getToAccount());
		final AccountDetailsDTO fromAccount = getAccountDetailsBasedOnAccountNumber(accountsFromCore,
				request.getFromAccount());

		validateAccountContext.add("from-account", fromAccount);
		validateAccountContext.add("to-account", toAccount);
		validateAccountContext.add("to-account-currency", request.getTxnCurrency());
		//TODO: ADD currency validation here
		BigDecimal transferAmountInSrcCurrency;
		if(isCurrencySame(request.getTxnCurrency(), fromAccount.getCurrency())) {
			transferAmountInSrcCurrency = request.getAmount();
		}else {
			CurrencyConversionDto conversionResult = getCurrencyExchangeObject(request.getAmount(), request, toAccount, fromAccount);
			transferAmountInSrcCurrency = conversionResult.getAccountCurrencyAmount();
		}

		validateAccountContext.add("transfer-amount-in-source-currency", transferAmountInSrcCurrency);

		// Limit Validation
		final BigDecimal limitUsageAmount = getLimitUsageAmount(fromAccount, transferAmountInSrcCurrency);
		limitValidatorFactory.getValidator(metaData).validate(userDTO, request.getServiceType(), limitUsageAmount, metaData, null);
		log.info("OwnAccountEligibility validation successfully finished");
		return EligibilityResponse.builder().status(FundsTransferEligibility.ELIGIBLE).build();
	}

	@Override
	public ServiceType getServiceType() {
		return ServiceType.WYMA;
	}

	private BigDecimal getLimitUsageAmount(final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		return LOCAL_CURRENCY.equalsIgnoreCase(sourceAccountDetailsDTO.getCurrency())
				? transferAmountInSrcCurrency
						: convertAmountInLocalCurrency(sourceAccountDetailsDTO, transferAmountInSrcCurrency);
	}

	private BigDecimal convertAmountInLocalCurrency(final AccountDetailsDTO sourceAccountDetailsDTO,
			final BigDecimal transferAmountInSrcCurrency) {
		CoreCurrencyConversionRequestDto currencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
		currencyConversionRequestDto.setAccountNumber(sourceAccountDetailsDTO.getNumber());
		currencyConversionRequestDto.setAccountCurrency(sourceAccountDetailsDTO.getCurrency());
		currencyConversionRequestDto.setAccountCurrencyAmount(transferAmountInSrcCurrency);
		currencyConversionRequestDto.setTransactionCurrency(LOCAL_CURRENCY);

		CurrencyConversionDto currencyConversionDto = maintenanceService.convertCurrency(currencyConversionRequestDto);
		return currencyConversionDto.getTransactionAmount();
	}

	private boolean isCurrencySame(String destinationCurrency, String sourceCurrency) {
		return destinationCurrency.equalsIgnoreCase(sourceCurrency);
	}

	//convert to sourceAccount from destAccount
	private CurrencyConversionDto getCurrencyExchangeObject(BigDecimal transactionAmount, FundTransferEligibiltyRequestDTO request, AccountDetailsDTO destAccount, AccountDetailsDTO sourceAccount) {
		final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
		currencyRequest.setAccountNumber(sourceAccount.getNumber());
		currencyRequest.setAccountCurrency(sourceAccount.getCurrency());
		currencyRequest.setTransactionCurrency(destAccount.getCurrency());
		currencyRequest.setDealNumber(request.getDealNumber());
		currencyRequest.setTransactionAmount(transactionAmount);
		return maintenanceService.convertBetweenCurrencies(currencyRequest);

	}

}
