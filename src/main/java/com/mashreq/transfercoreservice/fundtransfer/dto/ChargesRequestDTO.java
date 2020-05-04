package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 5/1/20
 */

@Data
@Builder
public class ChargesRequestDTO {

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
     * Debit Amount
     */
    private BigDecimal accountCurrencyAmount;

    /**
     * Bene ID
     */
    @NotNull
    private Long beneficiaryId;

    /**
     * Debit Currency
     */
    @NotEmpty
    private String accountCurrency;


}
