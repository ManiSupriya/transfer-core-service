package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

import java.util.Map;

/**
 * @author shahbazkh
 */
public interface Validator {

    default ValidationResult validate(final FundTransferRequestDTO request, final FundTransferMetadata metadata) {
        return validate(request, metadata, null);
    }

    ValidationResult validate(FundTransferRequestDTO request, FundTransferMetadata metadata, ValidationContext context);
}
