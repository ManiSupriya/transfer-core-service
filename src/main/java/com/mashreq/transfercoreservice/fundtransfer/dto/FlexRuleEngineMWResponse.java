package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 5/2/20
 */
@Data
@Builder
public class FlexRuleEngineMWResponse {

    private String productCode;
    private String chargeCurrency;
    private String chargeAmount;

    /**
     * Used only for INSTAREM
     */
    private BigDecimal transactionAmount;

    /**
     * Used only for INSTAREM
     */
    private BigDecimal accountCurrencyAmount;

    /**
     * Used only for INSTAREM
     */
    private BigDecimal exchangeRate;
}
