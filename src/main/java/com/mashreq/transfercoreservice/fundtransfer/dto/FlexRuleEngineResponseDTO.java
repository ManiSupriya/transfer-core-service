package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
@Builder
public class FlexRuleEngineResponseDTO {
    
    /**
     * Used for India and Pakistan
     */
    private String productCode;

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
