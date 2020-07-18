package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.event.FundTransferEventType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus.ACTIVE;
import static com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus.IN_COOLING_PERIOD;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * @author shahbazkh
 * @date 3/18/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryValidator implements Validator {

    private static final String QUICK_REMIT = "quick-remit";
    private final AsyncUserEventPublisher auditEventPublisher;


    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {

        final BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        log.info("Validating Beneficiary for service type [ {} ] ", htmlEscape(request.getServiceType()));

        if (beneficiaryDto == null) {
            auditEventPublisher.publishFailureEvent(FundTransferEventType.BENEFICIARY_VALIDATION, metadata, null,
                    BENE_NOT_FOUND.getCustomErrorCode(), BENE_NOT_FOUND.getErrorMessage(), null );
            return ValidationResult.builder().success(false).transferErrorCode(BENE_NOT_FOUND)
                    .build();
        }


        if (!beneficiaryDto.getAccountNumber().equals(request.getToAccount())) {
            auditEventPublisher.publishFailureEvent(FundTransferEventType.BENEFICIARY_VALIDATION, metadata, null,
                    BENE_ACC_NOT_MATCH.getCustomErrorCode(), BENE_ACC_NOT_MATCH.getErrorMessage(), null);
            return ValidationResult.builder().success(false).transferErrorCode(BENE_ACC_NOT_MATCH)
                    .build();
        }

        if (QUICK_REMIT.equals(request.getServiceType())) {
            return validateBeneficiaryStatus(Arrays.asList(ACTIVE.name(), IN_COOLING_PERIOD.name()),
                    beneficiaryDto.getStatus(), BENE_NOT_ACTIVE_OR_COOLING,metadata);
        }

        log.info("Beneficiary validation successful for service type [ {} ] ", htmlEscape(request.getServiceType()));
        return validateBeneficiaryStatus(Arrays.asList(ACTIVE.name()), beneficiaryDto.getStatus(), BENE_NOT_ACTIVE,metadata);
    }

    private ValidationResult validateBeneficiaryStatus(List<String> validStatus, String beneficiaryStatus, TransferErrorCode errorCode, RequestMetaData metadata) {
        if (validStatus.contains(beneficiaryStatus)) {
            auditEventPublisher.publishSuccessEvent(FundTransferEventType.BENEFICIARY_VALIDATION, metadata, null);
            return ValidationResult.builder().success(true).build();
        } else {
            auditEventPublisher.publishFailureEvent(FundTransferEventType.BENEFICIARY_VALIDATION, metadata, null,
                    errorCode.getCustomErrorCode(), errorCode.getErrorMessage(), null);
            return ValidationResult.builder().success(false).transferErrorCode(errorCode)
                    .build();
        }
    }


}
