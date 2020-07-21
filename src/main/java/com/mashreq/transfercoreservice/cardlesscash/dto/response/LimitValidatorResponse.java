package com.mashreq.transfercoreservice.cardlesscash.dto.response;

import java.math.BigDecimal;

import lombok.Data;
@Data
public class LimitValidatorResponse {

    private boolean isValid;
    private BigDecimal currentAvailableAmount;
    private BigDecimal currentAvailableCount;
    private String amountRemark;
    private String countRemark;
    private String trxReferanceNo;
}
