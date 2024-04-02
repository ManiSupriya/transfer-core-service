package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LimitValidatorRequest {
    private String cifId;
    private String trxType;
    private String countryCode;
    private String segment;
    private Long beneficiaryId;
    private BigDecimal payAmountInLocalCurrency;
}
