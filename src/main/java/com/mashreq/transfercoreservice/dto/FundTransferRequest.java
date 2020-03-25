package com.mashreq.transfercoreservice.dto;

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
    private BigDecimal amount;
    private String purposeCode;
    private String purposeDesc;
    private String chargeBearer;
    private String finTxnNo;
    private String sourceCurrency;
    private String sourceBranchCode;
    private String beneficiaryFullName;
    private String destinationBankName;
    private String swiftCode;
    private String routingCode;
}
