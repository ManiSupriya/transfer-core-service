package com.mashreq.transfercoreservice.soap.transfer;

import com.mashreq.transfercoreservice.soap.BaseMWRequest;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MashreqFundTransferMWRequest extends BaseMWRequest {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String purposeCode;
    private String dealNumber;
    private String fromAccountBranch;
    private String fromAccountCurrency;
    private BigDecimal exchangeRate;
}
