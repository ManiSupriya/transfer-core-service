package com.mashreq.transfercoreservice.fundtransfer.service;


import com.mashreq.mobcommons.config.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequestDTO;


public interface FundTransferService {

    Object transferFund(RequestMetaData fundTransferMetadata, FundTransferRequestDTO fundTransferRequestDTO);

}
