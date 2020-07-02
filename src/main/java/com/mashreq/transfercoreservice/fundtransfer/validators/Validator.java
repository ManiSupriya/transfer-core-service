package com.mashreq.transfercoreservice.fundtransfer.validators;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;

/**
 * @author shahbazkh
 */
public interface Validator {

    default ValidationResult validate(final FundTransferRequestDTO request, final RequestMetaData metadata) {
        return validate(request, metadata, null);
    }

    ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metadata, ValidationContext context);
}
