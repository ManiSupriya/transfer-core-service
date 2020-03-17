package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.UserDTO;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public interface FundTransferStrategy {

    // validate fin txn no
    // validate account numbers
    // find digital user
    // find user dto
    // validate beneficiary
    // validate limit

    void validate(FundTransferMetadata metadata, FundTransferRequestDTO request);

    default void validateFinTxnNumber(FundTransferRequestDTO requestDTO) {

    }


}
