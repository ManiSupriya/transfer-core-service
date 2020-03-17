package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.client.dto.CharityBeneficiaryDto;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author shahbazkh
 */


@Slf4j
@Component
public class AccountBelongsToCharityValidator implements Validator {

    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {
        CharityBeneficiaryDto charity = context.get("charity", CharityBeneficiaryDto.class);

        if (!request.getToAccount().equals(charity.getAccountNumber()))
            return ValidationResult.builder().success(false).transferErrorCode(TransferErrorCode.BENE_ACC_NOT_MATCH).build();

        return ValidationResult.builder().success(true).build();
    }
}
