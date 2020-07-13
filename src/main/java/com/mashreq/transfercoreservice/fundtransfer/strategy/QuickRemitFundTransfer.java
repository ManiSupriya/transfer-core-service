package com.mashreq.transfercoreservice.fundtransfer.strategy;


import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferResponse;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;
import com.mashreq.transfercoreservice.fundtransfer.validators.ValidationContext;

public interface QuickRemitFundTransfer extends FundTransferStrategy {

    default FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO) {
        return execute(request, metadata, userDTO, null);
    }

    FundTransferResponse execute(FundTransferRequestDTO request, RequestMetaData metadata, UserDTO userDTO, ValidationContext context);
}
