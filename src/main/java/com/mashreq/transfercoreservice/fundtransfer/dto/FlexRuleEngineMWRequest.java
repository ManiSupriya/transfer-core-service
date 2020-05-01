package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/29/20
 */

@Data
@Builder
public class FlexRuleEngineMWRequest {
    /**
     * Source Account Number
     */
    private String customerAccountNo;

    /**
     * Credit Currency
     */
    private String transactionCurrency;

    /**
     * Credit Amount
     */
    private BigDecimal transactionAmount;

    /**
     * Debit Currency
     */
    private String accountCurrency;

    /**
     * Debit Amount
     */
    private BigDecimal accountCurrencyAmount;

    /**
     * Defaults to AC
     */
    private String transferType;

    /**
     * Defaults to STP
     */
    private String transactionStatus;

    /**
     * Swift Code or XXXXIN for India
     */
    private String accountWithInstitution;

    /**
     *
     */
    private String valueDate;

    /**
     *
     */
    private String channelTraceId;
}
