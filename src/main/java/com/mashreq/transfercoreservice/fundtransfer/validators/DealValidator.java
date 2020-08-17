package com.mashreq.transfercoreservice.fundtransfer.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
			Date dealExpiryDate = null;
			try {
				dealExpiryDate = new SimpleDateFormat("dd/MM/yyyy").parse(dealEnquiryDetailsDto.getDealExpiryDate());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dealExpiryDate.compareTo(new Date()) < 0) {
				log.info("Deal Validation failed");
				auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
						CommonConstants.FUND_TRANSFER, TransferErrorCode.DEAL_VALIDATION_FAILED.toString(),
						TransferErrorCode.DEAL_VALIDATION_FAILED.getErrorMessage(),
						TransferErrorCode.DEAL_VALIDATION_FAILED.getErrorMessage());
				GenericExceptionHandler.handleError(TransferErrorCode.DEAL_VALIDATION_FAILED,
						TransferErrorCode.DEAL_VALIDATION_FAILED.getErrorMessage(),
						TransferErrorCode.DEAL_VALIDATION_FAILED.getErrorMessage());
				return ValidationResult.builder().success(false)
						.transferErrorCode(TransferErrorCode.DEAL_VALIDATION_FAILED).build();
			}
		}

		return ValidationResult.builder().success(true).build();
	}
}
