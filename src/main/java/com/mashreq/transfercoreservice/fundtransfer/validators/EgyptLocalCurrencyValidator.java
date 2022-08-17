package com.mashreq.transfercoreservice.fundtransfer.validators;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import lombok.extern.slf4j.Slf4j;

@Profile("egypt")
@Slf4j
@Component
public class EgyptLocalCurrencyValidator implements Validator<FundTransferRequestDTO> {


	private final LocalCurrencyValidations localCurrencyValidations;

	public EgyptLocalCurrencyValidator(LocalCurrencyValidations localCurrencyValidations) {
		this.localCurrencyValidations = localCurrencyValidations;
	}

	@Override
	public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata,
			ValidationContext context) {

		LocalCurrencyValidations.LocalCurrencyValidationRequest validationRequest = LocalCurrencyValidations.LocalCurrencyValidationRequest.builder()
				.requestMetaData(metadata)
				.transactionCurrency(request.getTxnCurrency())
				.validationContext(context)
				.serviceType(request.getServiceType())
				.build();

		return localCurrencyValidations.performLocalCurrencyChecks(validationRequest);
	}
}
