package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class QuickRemitFundTransferRequest {

    private String channelTraceId;//M
    private String finTxnNo;//M
    private String originatingCountry;//M
    private String destCountry;//M
    private String beneficiaryName;//M
    private String beneficiaryFullName;//M
    private String beneficiaryAddress;//M
    private String beneficiaryCountry;//O
    private String beneficiaryAccountNo;//M
    private String beneficiaryBankName;//M
    private String beneficiaryBankIFSC;//M
    private BigDecimal amountSRCCurrency;//O
    private BigDecimal amountDESTCurrency;//O
    private String srcCurrency;//M
    private String destCurrency;//M
    private String srcISOCurrency;//O
    private String destISOCurrency;//O
    private String transactionAmount;//M
    private String transactionCurrency;//M
    private BigDecimal exchangeRate;
    private String reasonCode;//M
    private String reasonText;//M
    private String senderName;//M
    private String senderMobileNo;//M
    private String senderBankBranch;
    private String senderBankAccount;//M
    private String senderAddress;//M
    private String senderCountryISOCode;//M
    private String senderIDType;//O
    private String senderIDNumber;//M
    private String serviceCode;
    private String beneficiaryIdType;
    private String beneficiaryIdNo;
    private String distributionType;
    private String transferType;
    private String beneficiaryMobileNo;
    private String beneficiaryBankCode;//MPK

}
