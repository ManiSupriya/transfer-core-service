package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.PurposeOfTransferDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PURPOSE_CODE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_PURPOSE_DESC;

@Slf4j
@Component
public class PaymentPurposeValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {
        final Set<PurposeOfTransferDto> purposes = context.get("purposes", Set.class);

        if(!lookForPurposeCode(purposes, request.getPurposeCode())) {
            return ValidationResult.builder().success(false).transferErrorCode(INVALID_PURPOSE_CODE)
                    .build();
        }

        if(!lookForPurposeDesc(purposes, request.getPurposeDesc())) {
            return ValidationResult.builder().success(false).transferErrorCode(INVALID_PURPOSE_DESC)
                    .build();
        }
        log.info("Purpose code and description validation successful");
        return ValidationResult.builder().success(true).build();
    }

    private boolean lookForPurposeCode(Set<PurposeOfTransferDto> purposes, String code) {
        return purposes.stream().anyMatch(pop -> pop.getPopCode().equals(code));
    }

    private boolean lookForPurposeDesc(Set<PurposeOfTransferDto> purposes, String desc) {
        return purposes.stream().anyMatch(pop -> pop.getPopDesc().equals(desc));
    }
}
