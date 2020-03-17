package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

/**
 * @author shahbazkh
 */
public class AccountBelongsToCharityValidator implements Validator{
    @Override
    public ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context) {
        return null;
    }
}
