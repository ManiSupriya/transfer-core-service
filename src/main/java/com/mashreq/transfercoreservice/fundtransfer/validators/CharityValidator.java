package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
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

        final CharityBeneficiaryDto charityBeneficiaryDto = context.get("charity-beneficiary-dto", CharityBeneficiaryDto.class);

        if (!request.getToAccount().equals(charityBeneficiaryDto.getAccountNumber()))
            return ValidationResult.builder().success(false).transferErrorCode(BENE_ACC_NOT_MATCH).build();

        return ValidationResult.builder().success(true).build();
    }
}
