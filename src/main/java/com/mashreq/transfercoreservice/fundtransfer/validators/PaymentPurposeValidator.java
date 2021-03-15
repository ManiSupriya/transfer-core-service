package com.mashreq.transfercoreservice.fundtransfer.validators;


import com.mashreq.mobcommons.services.events.publisher.AsyncUserEventPublisher;
import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.mobcommon.dto.MoneyTransferPurposeDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PURPOSE_CODE;
import static com.mashreq.transfercoreservice.event.FundTransferEventType.PAYMENT_PURPOSE_VALIDATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPurposeValidator implements Validator<FundTransferRequestDTO> {

    private final AsyncUserEventPublisher auditEventPublisher;

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context) {
        final Set<MoneyTransferPurposeDto> purposes = context.get("purposes", Set.class);

        MoneyTransferPurposeDto requestedPurpose = new MoneyTransferPurposeDto();
        requestedPurpose.setPurposeCode(request.getPurposeCode());
        requestedPurpose.setPurposeDesc(request.getPurposeDesc());


        if (!lookForPurposeCode(purposes, requestedPurpose)) {
            auditEventPublisher.publishFailureEvent(PAYMENT_PURPOSE_VALIDATION, metadata, null,
                    INVALID_PURPOSE_CODE.getCustomErrorCode(), INVALID_PURPOSE_CODE.getErrorMessage(), null);
            return ValidationResult.builder().success(false).transferErrorCode(INVALID_PURPOSE_CODE)
                    .build();
        }


        log.info("Purpose code and description validation successful");
        auditEventPublisher.publishSuccessEvent(PAYMENT_PURPOSE_VALIDATION, metadata, null);
        return ValidationResult.builder().success(true).build();
    }

    private boolean lookForPurposeCode(Set<MoneyTransferPurposeDto> purposes, MoneyTransferPurposeDto requestedPurpose) {
        return purposes.stream().anyMatch(pop -> pop.equals(requestedPurpose));
    }

}
