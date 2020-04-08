package com.mashreq.transfercoreservice.fundtransfer.limits;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LimitValidatorResultsDto {
    private String limitVersionUuid;
    private BigDecimal availableLimitAmount;
    private BigDecimal maxAmountDaily;
    private BigDecimal maxAmountMonthly;
    private String limitType;
}
