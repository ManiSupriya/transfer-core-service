package com.mashreq.transfercoreservice.fundtransfer.validators;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CC_TRX_NOT_ALLOWED;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.CREDIT_CARD_FUND_TRANSFER_REQUEST;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class CCTransactionEligibilityValidator implements Validator<FundTransferRequestDTO> {
	private final AsyncUserEventPublisher auditEventPublisher;
	@Override
	public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {
		log.debug("Credit card transaction eligibility started");
		if(StringUtils.isBlank(request.getToAccount()) && StringUtils.isNotBlank(request.getCardNo())) {
			if(!FTOrderType.PN.name().equals(request.getOrderType())) {
				return ValidationResult.builder().success(Boolean.FALSE).transferErrorCode(CC_TRX_NOT_ALLOWED).build();
			}
			ServiceType serviceByType = ServiceType.getServiceByType(request.getServiceType());
			return getEligibility(serviceByType,metadata);
        }
		return ValidationResult.builder().success(Boolean.TRUE).build();
	}

	private ValidationResult getEligibility(ServiceType serviceByType, RequestMetaData metadata) {
		ValidationResult result = null;
		switch (serviceByType) {
		case LOCAL:
			result = ValidationResult.builder().success(Boolean.TRUE).build();
			break;
		case WYMA:
			result = ValidationResult.builder().success(Boolean.FALSE).transferErrorCode(CC_TRX_NOT_ALLOWED).build();
			publishFailure(metadata);
			break;
		case WAMA:
			result = ValidationResult.builder().success(Boolean.FALSE).transferErrorCode(CC_TRX_NOT_ALLOWED).build();
			publishFailure(metadata);
			break;
		case INFT:
			result = ValidationResult.builder().success(Boolean.FALSE).transferErrorCode(CC_TRX_NOT_ALLOWED).build();
			publishFailure(metadata);
			break;
		default:
			result = ValidationResult.builder().success(Boolean.TRUE).transferErrorCode(CC_TRX_NOT_ALLOWED).build();
			break;
		}
		return result ;
	}

	protected void publishFailure(RequestMetaData metadata) {
		auditEventPublisher.publishFailureEvent(CREDIT_CARD_FUND_TRANSFER_REQUEST, metadata, null,
				CC_TRX_NOT_ALLOWED.getErrorMessage(), CC_TRX_NOT_ALLOWED.getErrorMessage(), null);
	}

}
