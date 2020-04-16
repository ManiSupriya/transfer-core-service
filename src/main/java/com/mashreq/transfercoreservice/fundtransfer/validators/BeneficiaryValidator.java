package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.client.dto.BeneficiaryStatus.ACTIVE;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;

/**
 * @author shahbazkh
 * @date 3/18/20
 */
@Slf4j
@Component
public class BeneficiaryValidator implements Validator {


    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {

        final BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        log.info("Validating Beneficiary for service type [ {} ] ", request.getServiceType());

        if (beneficiaryDto == null)
            return ValidationResult.builder().success(false).transferErrorCode(BENE_NOT_FOUND)
                    .build();

        if (!beneficiaryDto.getAccountNumber().equals(request.getToAccount()))
            return ValidationResult.builder().success(false).transferErrorCode(BENE_ACC_NOT_MATCH)
                    .build();

        if (!ACTIVE.getValue().equals(beneficiaryDto.getStatus()))
            return ValidationResult.builder().success(false).transferErrorCode(BENE_NOT_ACTIVE)
                    .build();

        log.info("Beneficiary validation successful for service type [ {} ] ", request.getServiceType());
        return ValidationResult.builder().success(true).build();
    }
}
