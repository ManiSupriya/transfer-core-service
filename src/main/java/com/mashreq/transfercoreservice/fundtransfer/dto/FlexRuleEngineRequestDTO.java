package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
@Builder
public class FlexRuleEngineRequestDTO {

    /**
     * Source Account Number
     */
    @NotEmpty
    private String customerAccountNo;

    /**
     * Credit Currency
     */
    @NotEmpty
    private String transactionCurrency;

    /**
     * Credit Amount
     */
    private BigDecimal transactionAmount;

    /**
     * Debit Currency
     */
    @NotEmpty
    private String accountCurrency;

    /**
     * Debit Amount
     */
    private BigDecimal accountCurrencyAmount;

    /**
     * Country Type
     */
    @NotNull
    private Long beneficiaryId;


}
