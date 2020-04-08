package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.BeneficiaryDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.SWIFT_CODE_EMPTY;

/**
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwiftCodeValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {
        final BeneficiaryDto beneficiaryDto = context.get("beneficiary-dto", BeneficiaryDto.class);
        if(StringUtils.isBlank(beneficiaryDto.getSwiftCode())) {
            log.info("Beneficiary Swift code is empty");
            return ValidationResult.builder().success(false).transferErrorCode(SWIFT_CODE_EMPTY)
                    .build();

        }
        log.info("Beneficiary Swift code validation successful");
        return ValidationResult.builder().success(true).build();
    }
}
