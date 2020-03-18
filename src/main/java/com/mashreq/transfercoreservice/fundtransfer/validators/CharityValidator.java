package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.BENE_ACC_NOT_MATCH;

/**
 * @author shahbazkh
 * @date 3/18/20
 */

@Slf4j
@Component
public class CharityValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        log.info("Validating charity beneficiary for service type [ {} ] ", request.getServiceType());
        final CharityBeneficiaryDto charityBeneficiaryDto = context.get("charity-beneficiary-dto", CharityBeneficiaryDto.class);

        if (!request.getToAccount().equals(charityBeneficiaryDto.getAccountNumber())) {
            log.warn("Charity Beneficiary not found for service type [ {} ] ", request.getServiceType());
            return ValidationResult.builder().success(false).transferErrorCode(BENE_ACC_NOT_MATCH).build();
        }

        log.info("Charity Beneficiary Validating successful service type [ {} ] ", request.getServiceType());
        return ValidationResult.builder().success(true).build();
    }
}
