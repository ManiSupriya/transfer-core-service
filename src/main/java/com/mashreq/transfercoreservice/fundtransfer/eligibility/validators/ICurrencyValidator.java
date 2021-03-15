package com.mashreq.transfercoreservice.fundtransfer.eligibility.validators;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;

public interface ICurrencyValidator {

	ValidationResult validate(FundTransferRequestDTO request, RequestMetaData metaData, ValidationContext validationContext);

}
