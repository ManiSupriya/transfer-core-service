package com.mashreq.transfercoreservice.fundtransfer.validators;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.mashreq.esbcore.bindings.account.mbcdm.FundTransferResType;
import com.mashreq.esbcore.bindings.header.mbcdm.ErrorType;
import com.mashreq.esbcore.validator.EsbResponseValidator;
import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.client.dto.FundTransferMWResponse;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.AccountDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.service.AccountDetailsEaiServices;
import com.mashreq.transfercoreservice.fundtransfer.service.FundTransferEaiServices;
import com.mashreq.transfercoreservice.middleware.EsbRequestsCreator;
import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.transfercoreservice.middleware.WebServiceClient;
import com.mashreq.transfercoreservice.middleware.enums.MwResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealValidator implements Validator {

	private final AsyncUserEventPublisher auditEventPublisher;
	private final MaintenanceService maintenanceService;
	private final EsbRequestsCreator esbRequestsCreator;
	private final WebServiceClient webServiceClient;
	private final SoapServiceProperties appProperties;
	private static final String SUCCESS = "S";
	private static final String SUCCESS_CODE_ENDS_WITH = "-000";
	private final ConversionService conversionService;

	@Override
	public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata,
			ValidationContext context) {
		 log.info("Deal Validation Started");
		final BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
		Optional<SearchAccountDto> fromAccount = getSearchAccountDto(request.getFromAccount(), metadata);
		final CoreCurrencyConversionRequestDto currencyRequest = new CoreCurrencyConversionRequestDto();
		currencyRequest.setAccountNumber(request.getFromAccount());
		currencyRequest.setAccountCurrency(request.getCurrency());
		currencyRequest.setTransactionCurrency(beneficiaryDto.getBeneficiaryCurrency());
		currencyRequest.setTransactionAmount(request.getSrcAmount());
		CurrencyConversionDto currencyConversionDto = getConversionRate(fromAccount.get(), request.getAmount(),
				request.getCurrency(), request.getDealNumber());
		validateAccountForTransfer(fromAccount.get(), currencyConversionDto, metadata);
		FundTransferEaiServices req = esbRequestsCreator.createFundTransferRequest(request.getFromAccount(),
				request.getAmount(), request.getCurrency(), request.getToAccount(), beneficiaryDto.getBankBranchName(),
				request.getCurrency(), request.getDealNumber(), currencyConversionDto);
		FundTransferEaiServices resp = new FundTransferEaiServices(webServiceClient.exchange(req));

		FundTransferMWResponse fundTransferMWResponse = new FundTransferMWResponse();
		fundTransferMWResponse.setMwResponseStatus(MwResponseStatus.F);

		if (isMwResponseSuccess(resp)) {
			log.info("[AccountService] Fund transfer MW return successful response");
			FundTransferResType.Transfer transferResp = resp.getBody().getFundTransferRes().getTransfer().get(0);
			fundTransferMWResponse.setTransactionRefNo(transferResp.getTransactionRefNo());
			fundTransferMWResponse.setMwResponseStatus(MwResponseStatus.S);
			log.info("Deal Validation successful");
		    auditEventPublisher.publishSuccessEvent(FundTransferEventType.DEAL_VALIDATION, metadata, CommonConstants.FUND_TRANSFER);
			return ValidationResult.builder().success(true).build();
		}
		ErrorType errorType = resp.getBody().getExceptionDetails();
		fundTransferMWResponse.setMwReferenceNo(errorType.getReferenceNo());
		fundTransferMWResponse.setMwResponseCode(errorType.getErrorCode());
		fundTransferMWResponse.setMwResponseDescription(errorType.getErrorDescription());

		 log.info("Deal Validation failed");
		 auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata, CommonConstants.FUND_TRANSFER,
				 errorType.getErrorCode(),errorType.getErrorDescription(), errorType.getErrorDescription());
		 GenericExceptionHandler.handleError(TransferErrorCode.DEAL_VALIDATION_FAILED, TransferErrorCode.DEAL_VALIDATION_FAILED.getErrorMessage()+"-"+errorType.getErrorCode(),errorType.getErrorDescription());
         return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.DEAL_VALIDATION_FAILED)
                 .build(); 
	}

	private CurrencyConversionDto getConversionRate(SearchAccountDto account, BigDecimal amount, String currency,
			String dealNumber) {
		CoreCurrencyConversionRequestDto coreCurrencyConversionRequestDto = new CoreCurrencyConversionRequestDto();
		coreCurrencyConversionRequestDto.setAccountCurrency(currency);
		coreCurrencyConversionRequestDto.setAccountCurrencyAmount(amount);
		if (!Objects.equals(account.getCurrency(), currency)) {
			return maintenanceService.convertBetweenCurrencies(coreCurrencyConversionRequestDto);
		} else {
			CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
			currencyConversionDto.setAccountCurrencyAmount(amount);
			currencyConversionDto.setExchangeRate(BigDecimal.ONE);
			currencyConversionDto.setTransactionAmount(amount);
			return currencyConversionDto;
		}
	}

	private Optional<SearchAccountDto> getSearchAccountDto(String accountNumber, RequestMetaData metadata) {
		AccountDetailsDto accountDetails = getAccountDetails(accountNumber, metadata);
		if (accountDetails == null || CollectionUtils.isEmpty(accountDetails.getConnectedAccounts())) {
			return Optional.empty();
		}
		return accountDetails.getConnectedAccounts().stream().filter(a -> a.getNumber().equals(accountNumber))
				.findAny();
	}

	private boolean isMwResponseSuccess(FundTransferEaiServices response) {
		log.debug("Validate MW response {}", response);
		if (!(StringUtils.endsWith(response.getBody().getExceptionDetails().getErrorCode(), SUCCESS_CODE_ENDS_WITH)
				&& SUCCESS.equals(response.getHeader().getStatus()))) {
			return false;
		}
		return true;
	}

	/**
	 * Returns account details
	 */
	public AccountDetailsDto getAccountDetails(String accNo, RequestMetaData metadata) {
		AccountDetailsEaiServices request = esbRequestsCreator.createEsbAccountDetaisRequest(accNo);
		AccountDetailsEaiServices resp = new AccountDetailsEaiServices(webServiceClient.exchange(request));
		EsbResponseValidator.validate(
				appProperties.getServiceCodes().getSearchAccountDetails(),
				CommonConstants.SEARCH_ACCOUNT_DETAILS, resp, null);
		return auditEventPublisher.publishEventLifecycle(
                () -> conversionService.convert(resp.getBody().getAccountDetailsRes(),
				AccountDetailsDto.class),FundTransferEventType.DEAL_VALIDATION, metadata, CommonConstants.FUND_TRANSFER);
	}

	private void validateAccountForTransfer(SearchAccountDto account, CurrencyConversionDto currencyConversionDto, RequestMetaData metadata) {
		if (currencyConversionDto.getAccountCurrencyAmount()
				.compareTo(new BigDecimal(account.getAvailableBalance())) > 0) {
			log.error("There is not enough resources in account " + account.getNumber() + " to transfer "
					+ currencyConversionDto.getAccountCurrencyAmount());
			auditEventPublisher.publishFailedEsbEvent(FundTransferEventType.RESOURCE_NOT_MACTH, metadata, CommonConstants.FUND_TRANSFER, metadata.getChannelTraceId(),
         			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.toString(), TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage(),
       			TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
			 GenericExceptionHandler.handleError(TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH, TransferErrorCode.ACCOUNT_RESOURCES_MISMATCH.getErrorMessage());
		}
	}

}
