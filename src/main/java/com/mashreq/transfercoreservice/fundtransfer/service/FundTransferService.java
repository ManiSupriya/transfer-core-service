package com.mashreq.transfercoreservice.fundtransfer.service;


import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;
import com.mashreq.transfercoreservice.fundtransfer.dto.RequestMetaData;


public interface FundTransferService {

    Object transferFund(RequestMetaData fundTransferMetadata, FundTransferRequestDTO fundTransferRequestDTO);

}
