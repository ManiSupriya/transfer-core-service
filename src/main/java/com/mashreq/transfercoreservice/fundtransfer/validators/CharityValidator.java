package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.event.publisher.AsyncUserEventPublisher;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_ACC_NOT_MATCH;
import static com.mashreq.transfercoreservice.event.model.EventType.CHARITY_ACCOUNT_VALIDATION;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CharityValidator implements Validator {

    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        log.info("Validating charity beneficiary for service type [ {} ] ", request.getServiceType());
        final CharityBeneficiaryDto charityBeneficiaryDto = context.get("charity-beneficiary-dto", CharityBeneficiaryDto.class);

        if (!request.getToAccount().equals(charityBeneficiaryDto.getAccountNumber())) {
            log.warn("Charity Beneficiary not found for service type [ {} ] ", request.getServiceType());
            auditEventPublisher.publishFailureEvent(CHARITY_ACCOUNT_VALIDATION, metadata, null,
                    BENE_ACC_NOT_MATCH.getCustomErrorCode(), BENE_ACC_NOT_MATCH.getErrorMessage(), null);
            return ValidationResult.builder().success(false).transferErrorCode(BENE_ACC_NOT_MATCH).build();
        }

        log.info("Charity Beneficiary Validating successful service type [ {} ] ", request.getServiceType());
        auditEventPublisher.publishSuccessEvent(CHARITY_ACCOUNT_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }
}
