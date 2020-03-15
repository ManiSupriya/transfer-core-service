package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public class OwnAccountStrategy implements FundTransferStrategy {

    @Override
    public PaymentHistoryDTO transferFund(FundTransferMetadata fundTransferMetadata, FundTransferRequestDTO fundTransferRequestDTO) {
        //account sanity
        //unique fin transaction number
        //same account should fail
        //account exists and not close
        //balance
        //limit
        return null;
    }
}
