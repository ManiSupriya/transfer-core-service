package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.transfercoreservice.client.dto.CoreFundTransferResponseDto;
import com.mashreq.transfercoreservice.fundtransfer.dto.PaymentHistoryDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;


public interface FundTransferService {

    CoreFundTransferResponseDto transferFund(FundTransferRequestDTO fundTransferRequestDTO);

}
