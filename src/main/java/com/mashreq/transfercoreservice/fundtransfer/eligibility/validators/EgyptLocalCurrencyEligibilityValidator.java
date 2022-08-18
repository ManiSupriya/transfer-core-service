package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.mobcommon.MobCommonClient;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.LocalCurrencyValidations;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

import lombok.extern.slf4j.Slf4j;

@Profile("egypt")
@Slf4j
@Component("currencyValidatorEligibility")
public class EgyptLocalCurrencyEligibilityValidator extends CurrencyValidator {

	private final LocalCurrencyValidations localCurrencyValidations;

	public EgyptLocalCurrencyEligibilityValidator(LocalCurrencyValidations localCurrencyValidations, 
			MobCommonClient mobCommonClient, AsyncUserEventPublisher auditEventPublisher) {
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
		
		return localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
	}
}
