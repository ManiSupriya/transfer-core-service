package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class FundTransferRequest {

    private String channel;
    private String channelTraceId;
    private String fromAccount;
    private String toAccount;
    private String productId;
    private BigDecimal amount;
    private String purposeCode;
    private String purposeDesc;
    private String chargeBearer;
    private String finTxnNo;
    private String sourceCurrency;
    private String destinationCurrency;
    private String sourceBranchCode;
    private String beneficiaryFullName;
    private String awInstBICCode;
    private String awInstName;
    private String beneficiaryAddressOne;
    private String beneficiaryAddressTwo;
    private String beneficiaryAddressThree;
    private String transactionCode;
    private String internalAccFlag;
    private BigDecimal srcAmount;

}
