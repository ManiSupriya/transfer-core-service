package com.mashreq.transfercoreservice.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharityPaidDto {
    private BigDecimal totalPaidAmount;
}
