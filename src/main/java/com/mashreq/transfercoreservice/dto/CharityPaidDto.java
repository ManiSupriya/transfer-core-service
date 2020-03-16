package com.mashreq.transfercoreservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CharityPaidDto {
    private BigDecimal totalPaidAmount;
}
