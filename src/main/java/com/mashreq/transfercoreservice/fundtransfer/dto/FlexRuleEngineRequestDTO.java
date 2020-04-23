package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
public class FlexRuleEngineRequestDTO {
    private String customerAccountNo;
    private String transactionCurrency;
    private BigDecimal transactionAmount;
    private FlexRuleEngineCountryType countryType;
}
