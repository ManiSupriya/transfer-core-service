package com.mashreq.transfercoreservice.fundtransfer.eligibility.service;


import java.util.List;
import java.util.Objects;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.errors.ExceptionUtils;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import com.mashreq.transfercoreservice.fundtransfer.dto.CustomerClientType;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferEligibiltyRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.eligibility.dto.EligibilityResponse;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationResult;


public interface TransferEligibilityService {

    EligibilityResponse checkEligibility(RequestMetaData metaData, FundTransferEligibiltyRequestDTO request, UserDTO userDTO);
    
    ServiceType getServiceType();

    default void responseHandler(ValidationResult validationResult) {
        if (!validationResult.isSuccess()) {
            GenericExceptionHandler.handleError(validationResult.getTransferErrorCode(), validationResult.getTransferErrorCode().getErrorMessage());
        }
    }

    default AccountDetailsDTO getAccountDetailsBasedOnAccountNumber(List<AccountDetailsDTO> coreAccounts, String accountNumber) {
        return coreAccounts.stream()
                .filter(account -> account.getNumber().equals(accountNumber))
                .findFirst().orElseThrow(() -> ExceptionUtils.genericException(TransferErrorCode.INVALID_ACCOUNT_NUMBER));
    }
    
    default boolean isSMESegment(RequestMetaData metaData) {
		if(Objects.nonNull(metaData.getUserType()) && metaData.getUserType().equals(CustomerClientType.SME.name())) {
			return true;
		}
		return false;
	}

	default void modifyServiceType(FundTransferEligibiltyRequestDTO request) {
		request.setServiceType(getServiceType().name());
	}
}
