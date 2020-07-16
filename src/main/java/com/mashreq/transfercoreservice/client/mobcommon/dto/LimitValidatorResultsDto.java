package com.mashreq.transfercoreservice.client.mobcommon.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class LimitValidatorResultsDto {
    private boolean isValid;
    private String limitVersionUuid;
    private BigDecimal availableLimitAmount;
    private BigDecimal maxAmountDaily;
    private BigDecimal maxAmountMonthly;
    private LimitCheckType limitCheckType;
}
