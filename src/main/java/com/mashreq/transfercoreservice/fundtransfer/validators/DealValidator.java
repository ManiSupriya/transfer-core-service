package com.mashreq.transfercoreservice.fundtransfer.validators;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealEnquiryDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealEnquiryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealValidator implements Validator {

	private final AsyncUserEventPublisher auditEventPublisher;
	private final MaintenanceService maintenanceService;

	@Override
	public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata,
			ValidationContext context) {
		log.info("Deal Validation Started");
		final DealEnquiryDto dealEnquiryDto = maintenanceService.getFXDealInformation(request.getDealNumber());
		for (DealEnquiryDetailsDto dealEnquiryDetailsDto : dealEnquiryDto.getDetailsDtoList()) {
   		 LocalDate localDate = LocalDate.parse(dealEnquiryDetailsDto.getDealExpiryDate());
			if(dealEnquiryDetailsDto.getDealAmount().compareTo(request.getAmount()) == -1) {
				log.info("Deal Validation failed");	
				auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
						CommonConstants.DEAL_VALIDATION, TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.toString(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage(),
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE,
						TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage(),TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage());
			}
			if (localDate.isBefore(LocalDate.now())) {
				log.info("Deal Validation failed");	
				auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
						CommonConstants.DEAL_VALIDATION, TransferErrorCode.DEAL_NUMBER_EXPIRED.toString(),
						TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage(),
						TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_EXPIRED,
						TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage(),TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage());
			}
		}

		return ValidationResult.builder().success(true).build();
	}
}
