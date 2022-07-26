package com.mashreq.transfercoreservice.fundtransfer.validators;

import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.ACCOUNT_CURRENCY_MISMATCH;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CURRENCY_IS_INVALID;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.LOCAL;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WAMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.WYMA;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType.INFT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Profile("egypt")
@Slf4j
@Component
public class EgyptLocalCurrencyValidator extends CurrencyValidator {


	private final LocalCurrencyValidations localCurrencyValidations;

	public EgyptLocalCurrencyValidator(
									   AsyncUserEventPublisher auditEventPublisher,
									   LocalCurrencyValidations localCurrencyValidations) {
		super(auditEventPublisher);
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
		ValidationResult validationResult = localCurrencyValidations.performLocalCurrencyChecks(validationRequest);

		return Objects.nonNull(validationResult) ? validationResult : super.validate(request, metadata, context);
	}



}
