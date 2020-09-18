package com.mashreq.transfercoreservice.fundtransfer.validators;

import static com.mashreq.transfercoreservice.common.CommonConstants.INVALID_DEAL_NUMBER;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericException;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.ErrorUtils;
import com.mashreq.transfercoreservice.client.service.MaintenanceService;
import com.mashreq.transfercoreservice.common.CommonConstants;
import com.mashreq.transfercoreservice.errors.ExternalErrorCodeConfig;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealEnquiryDetailsDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.DealEnquiryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.webcore.dto.response.Response;

import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealValidator implements Validator {

	private final AsyncUserEventPublisher auditEventPublisher;
	private final MaintenanceService maintenanceService;
	private final ExternalErrorCodeConfig errorCodeConfig;

	@Override
	public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata,
			ValidationContext context) {
		DealEnquiryDto dealEnquiryDto = null ;
		try {
			if (request.getDealNumber() != null) {
				dealEnquiryDto = maintenanceService.getFXDealInformation(request.getDealNumber());
				for (DealEnquiryDetailsDto dealEnquiryDetailsDto : dealEnquiryDto.getDetailsDtoList()) {
					LocalDate localDate = LocalDate.parse(dealEnquiryDetailsDto.getDealExpiryDate());
					if (dealEnquiryDetailsDto.getDealAmount().compareTo(request.getAmount()) == -1) {
						log.info("Deal Validation failed");
						auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
								CommonConstants.DEAL_VALIDATION,
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.toString(),
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage(),
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage());
						GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE,
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage(),
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage());
					}
					if (localDate.isBefore(LocalDate.now())) {
						log.info("Deal Validation failed");
						auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
								CommonConstants.DEAL_VALIDATION, TransferErrorCode.DEAL_NUMBER_EXPIRED.toString(),
								TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage(),
								TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage());
						GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_EXPIRED,
								TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage(),
								TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage());
					}
					if (!StringUtils.equalsIgnoreCase(dealEnquiryDetailsDto.getSellCurrency(), request.getCurrency())) {
						log.info("Deal Validation failed");
					auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
							CommonConstants.DEAL_VALIDATION, TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.toString(),
							TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.getErrorMessage(),
							TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.getErrorMessage());
						GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY,
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.getErrorMessage(),
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.getErrorMessage());
					}

					if (!StringUtils.equalsIgnoreCase(dealEnquiryDetailsDto.getBuyCurrency(),
							request.getTxnCurrency())) {log.info("Deal Validation failed");
							auditEventPublisher.publishFailureEvent(FundTransferEventType.DEAL_VALIDATION, metadata,
									CommonConstants.DEAL_VALIDATION, TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.toString(),
									TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.getErrorMessage(),
									TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.getErrorMessage());
						GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY,
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.getErrorMessage(),
								TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.getErrorMessage());
					}
					request.setDealRate(dealEnquiryDetailsDto.getDealRate());
				}
			}
		} catch (GenericException e) {
			handleErrorResponse(e);
		}

		return ValidationResult.builder().success(true).build();
	}
	private Map<String, String> errorMap() {
		 return errorCodeConfig.getDealDetailsExternalErrorCodesMap();
	}
	private TransferErrorCode assignFeignConnectionErrorCode() {
		 return TransferErrorCode.MAINTENANCE_SERVICE_CONNECTION_ERROR;
	}
	public void assignCustomErrorCode(String errorDetail, TransferErrorCode errorCode) {
		GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage());
	}
	
	 private ResponseEntity<Response<DealEnquiryDto>> handleErrorResponse(Throwable throwable) {
	        if (throwable instanceof GenericException) {
	            GenericException genericException = (GenericException) throwable;
	            final String[] errorCodes = ErrorUtils.getAllErrorCodesFromGenericException(genericException);
	            final String errorDetails = genericException.getErrorDetails();
	            final Optional<String> errorHandlingStrategyOptional = ErrorUtils.getErrorHandlingStrategy(errorMap(), errorCodes);

	            if (errorHandlingStrategyOptional.isPresent()) {
	                final String errorHandlingStrategy = errorHandlingStrategyOptional.get();
	                if (INVALID_DEAL_NUMBER.equals(errorHandlingStrategy)) {
	                	if (INVALID_DEAL_NUMBER.equals(errorHandlingStrategy)) {
	                    	GenericExceptionHandler.handleError(TransferErrorCode.INVALID_DEAL_NUMBER, TransferErrorCode.INVALID_DEAL_NUMBER.getErrorMessage(), TransferErrorCode.INVALID_DEAL_NUMBER.getErrorMessage());
	                    }
	                } else {
	                	TransferErrorCode errorCode = TransferErrorCode.valueOf(errorHandlingStrategy);
	                    GenericExceptionHandler.handleError(errorCode, errorCode.getErrorMessage(), errorDetails);
	                }
	            }
	            if(StringUtils.equalsAnyIgnoreCase(genericException.getErrorCode(), TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getCustomErrorCode()))
	            	GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE,TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE.getErrorMessage());
	            if(StringUtils.equalsAnyIgnoreCase(genericException.getErrorCode(), TransferErrorCode.DEAL_NUMBER_EXPIRED.getCustomErrorCode()))
	            	GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_EXPIRED, TransferErrorCode.DEAL_NUMBER_EXPIRED.getErrorMessage());
	            if(StringUtils.equalsAnyIgnoreCase(genericException.getErrorCode(), TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.getCustomErrorCode()))
	            	GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY, TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_SRC_CRNCY.getErrorMessage());
	            if(StringUtils.equalsAnyIgnoreCase(genericException.getErrorCode(), TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.getCustomErrorCode()))
	            	GenericExceptionHandler.handleError(TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY, TransferErrorCode.DEAL_NUMBER_NOT_APPLICABLE_WITH_TXN_CRNCY.getErrorMessage());
	        }
	        if(throwable instanceof RetryableException) {
         	GenericExceptionHandler.handleError(assignFeignConnectionErrorCode(), assignFeignConnectionErrorCode().getErrorMessage(), assignFeignConnectionErrorCode().getErrorMessage());
         } 
	        GenericExceptionHandler.handleError(TransferErrorCode.MAINTENANCE_SERVICE_ERROR, TransferErrorCode.MAINTENANCE_SERVICE_ERROR.getErrorMessage(), TransferErrorCode.MAINTENANCE_SERVICE_ERROR.getErrorMessage());
			return null;
	    }
}
