package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LimitValidatorResponse {

    private boolean isValid;
    private BigDecimal currentAvailableAmount;
    private BigDecimal currentAvailableCount;
    private String amountRemark;
    private String countRemark;
    private String transactionRefNo;
    private String dailyUsedAmount;
    private String maxAmountMonthly;
    private String monthlyUsedCount;
    private String maxCountMonthly;
    private String dailyUsedCount;
    private String maxCountDaily;
    private String maxTrxAmout;
    private String maxAmountDaily;
    private String monthlyUsedAmount;
    private String coolingLimitCount;
    private String coolingLimitAmount;
    private String limitVersionUuid;
}
