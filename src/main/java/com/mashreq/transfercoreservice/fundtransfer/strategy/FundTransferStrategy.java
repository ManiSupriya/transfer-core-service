package com.mashreq.transfercoreservice.fundtransfer.strategy;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public interface FundTransferStrategy {

    PaymentHistoryDTO transferFund(FundTransferMetadata fundTransferMetadata, FundTransferRequestDTO fundTransferRequestDTO);
}
