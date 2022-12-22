package com.mashreq.transfercoreservice.fundtransfer.service;

import com.mashreq.mobcommons.services.http.RequestMetaData;
import com.mashreq.transfercoreservice.client.dto.TransactionChargesDto;
import com.mashreq.transfercoreservice.client.service.BankChargesService;
import com.mashreq.transfercoreservice.errors.ExceptionUtils;
import com.mashreq.transfercoreservice.fundtransfer.dto.FundTransferRequest;
import com.mashreq.transfercoreservice.fundtransfer.dto.ServiceType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.INVALID_CHARGE_BEARER;
import static com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer.getChargeBearerByName;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/***
 * @author shilpin
 */
@Profile("!egypt")
@Service
public class UAEBankChargesService implements TransferBankChargesService {
    @Autowired
    private BankChargesService bankChargesService;

    @Override
    public String getBankFeesForCustomerByCharge(FundTransferRequest fundTransferRequest, RequestMetaData requestMetaData,ServiceType type) {
        if(StringUtils.isBlank(fundTransferRequest.getChargeBearer())){
            throw ExceptionUtils.genericException(INVALID_CHARGE_BEARER,INVALID_CHARGE_BEARER.getErrorMessage());
        }
        final TransactionChargesDto bankCharges = bankChargesService.getTransactionCharges(fundTransferRequest.getAccountClass(), fundTransferRequest.getTxnCurrency(), requestMetaData);
        String charges = EMPTY;
        Double bankCharge = ServiceType.INFT.equals(type) ? bankCharges.getInternationalTransactionalCharge() : bankCharges.getLocalTransactionCharge();
        switch(getChargeBearerByName(fundTransferRequest.getChargeBearer())){
            case U:
                charges = String.valueOf(bankCharge);
                break;
            case O:
                charges = String.valueOf(bankCharge);
                break;
            case B:
                break;
            default:
                throw ExceptionUtils.genericException(INVALID_CHARGE_BEARER,INVALID_CHARGE_BEARER.getErrorMessage());
        }
        return charges;
    }
}
