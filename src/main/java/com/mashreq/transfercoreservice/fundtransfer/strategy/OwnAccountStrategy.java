package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public class OwnAccountStrategy implements FundTransferStrategy {


    @Override
    public void validate(FundTransferMetadata metadata, FundTransferRequestDTO request) {

        // validate fin txn no
        // validate account numbers
        // find digital user
        // find user dto
        // validate beneficiary
        // validate limit

    }
}
