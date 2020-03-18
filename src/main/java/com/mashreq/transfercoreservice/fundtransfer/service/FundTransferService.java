package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferMetadata;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;


public interface FundTransferService {

    PaymentHistoryDTO transferFund(FundTransferMetadata fundTransferMetadata, FundTransferRequestDTO fundTransferRequestDTO);

}
