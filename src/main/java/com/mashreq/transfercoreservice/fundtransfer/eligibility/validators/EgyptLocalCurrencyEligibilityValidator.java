package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;

import com.mashreq.transfercoreservice.fundtransfer.validators.LocalCurrencyValidations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Profile("egypt")
@Slf4j
@Component("currencyValidatorEligibility")
public class EgyptLocalCurrencyEligibilityValidator extends CurrencyValidator {

	private final LocalCurrencyValidations localCurrencyValidations;

	public EgyptLocalCurrencyEligibilityValidator(AsyncUserEventPublisher auditEventPublisher, MobCommonClient mobCommonClient,
												  LocalCurrencyValidations localCurrencyValidations) {
		super(auditEventPublisher, mobCommonClient);
		this.localCurrencyValidations = localCurrencyValidations;

	}
	
	@Override
    public ValidationResult validate(FundTransferEligibiltyRequestDTO request, RequestMetaData metadata, ValidationContext context) {

		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(request.getTxnCurrency())
				.validationContext(context)
				.serviceType(request.getServiceType())
				.build();
		ValidationResult validationResult = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
		return Objects.nonNull(validationResult) ? validationResult : super.validate(request, metadata, context);
	}


}
