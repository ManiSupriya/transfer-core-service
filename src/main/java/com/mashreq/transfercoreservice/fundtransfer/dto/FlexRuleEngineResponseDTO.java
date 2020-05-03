package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
