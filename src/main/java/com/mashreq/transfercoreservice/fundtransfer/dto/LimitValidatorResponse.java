package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LimitValidatorResponse {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isValid;

    private BigDecimal currentAvailableAmount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer currentAvailableCount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String amountRemark;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String countRemark;

    private String transactionRefNo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String dailyUsedAmount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String maxAmountMonthly;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String monthlyUsedCount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String maxCountMonthly;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String dailyUsedCount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String maxCountDaily;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String maxTrxAmount;

    private String maxAmountDaily;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String monthlyUsedAmount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String coolingLimitCount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String coolingLimitAmount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String limitVersionUuid;

    private String errorCode;

    private String frequencyPerMonth;
    private BigDecimal segmentLimit;
    private BigDecimal threshold;
    private String limitChangeWindow;
    private Integer limitFreezeHoursTimer;
    private String verificationType;
}
