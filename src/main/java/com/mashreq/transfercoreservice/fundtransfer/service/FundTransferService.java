package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;


public interface FundTransferService {

    PaymentHistoryDTO transferFund(FundTransferRequestDTO fundTransferRequestDTO);

}
