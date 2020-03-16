package com.mashreq.transfercoreservice.limits;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LimitValidatorResultsDto {
    private final boolean isValid;
    private String limitVersionUuid;
    private BigDecimal availableLimitAmount;
    private Integer availableLimitCount;
}
