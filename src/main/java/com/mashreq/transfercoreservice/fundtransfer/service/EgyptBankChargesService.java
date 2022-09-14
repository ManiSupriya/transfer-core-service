package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/***
 * @author shilpin
 */
@Profile("egypt")
@Service
public class EgyptBankChargesService implements TransferBankChargesService {
    @Override
    public String getBankFeesForCustomerByCharge(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData, ServiceType type) {
        return EMPTY;
    }
}
