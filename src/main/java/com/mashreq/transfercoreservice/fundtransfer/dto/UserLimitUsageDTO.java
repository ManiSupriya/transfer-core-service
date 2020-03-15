package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserLimitUsageDTO {
    private Integer usedCount;
    private BigDecimal usedAmount;
}

