package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;

/***
 * @author shilpin
 */
public interface TransferBankChargesService {
    String getBankFeesForCustomerByCharge(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData, ServiceType type);
}
