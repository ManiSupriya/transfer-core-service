package com.mashreq.transfercoreservice.paylater.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money implements Serializable {
    @Column(name="trans_ccy",nullable = false,length = 3)
    private Currency currency;
    @Column(name="trans_amnt",nullable = false,precision = 2)
    private BigDecimal amount;

    public Money(Currency currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), 2);
    }

    public static Money valueOf(String transactionAmount, String transactionCurrency) {
        return new Money(Currency.getInstance(transactionCurrency), new BigDecimal(transactionAmount));
    }

    public static Money valueOf(BigDecimal transactionAmount, String transactionCurrency) {
        return new Money(Currency.getInstance(transactionCurrency), transactionAmount);
    }

    public String getTransactionValueAsString() {
        return StringUtils.join(Arrays.asList(amount, currency), " ");
    }
}
