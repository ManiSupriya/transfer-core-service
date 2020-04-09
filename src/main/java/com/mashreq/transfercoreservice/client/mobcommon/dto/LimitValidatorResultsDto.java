package com.mashreq.transfercoreservice.client.mobcommon.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LimitValidatorResultsDto {
    private boolean isValid;
    private String limitVersionUuid;
    private BigDecimal availableLimitAmount;
    private BigDecimal maxAmountDaily;
    private BigDecimal maxAmountMonthly;
    private LimitCheckType limitCheckType;
}
