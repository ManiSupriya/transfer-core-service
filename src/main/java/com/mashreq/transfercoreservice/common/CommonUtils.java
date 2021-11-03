package com.mashreq.transfercoreservice.common;

import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class CommonUtils {

    public static String generateDisplayString(CurrencyConversionDto conversionResult, CoreCurrencyConversionRequestDto conversionRequestDto) {
        try {
            StringBuilder builder = new StringBuilder("");
            BigDecimal exchangeRate = conversionResult.getAccountCurrencyAmount().divide(conversionResult.getTransactionAmount(),8, RoundingMode.HALF_UP);
            if (exchangeRate.compareTo(BigDecimal.ONE) > 0) {
                return builder.append("1 ").append(conversionRequestDto.getTransactionCurrency()).append(" = ")
                        .append(exchangeRate.setScale(5, RoundingMode.DOWN).toPlainString()).append(" ")
                        .append(conversionRequestDto.getAccountCurrency()).toString();
            } else {
                exchangeRate = findReciprocal(exchangeRate);
                return builder.append("1 ").append(conversionRequestDto.getAccountCurrency()).append(" = ")
                        .append(exchangeRate.setScale(5, RoundingMode.DOWN).toPlainString()).append(" ")
                        .append(conversionRequestDto.getTransactionCurrency()).toString();
            }
        } catch (Exception e) {
            log.error("Error while preparing exchange rate display string", e);
        }
        return null;
    }

    private static BigDecimal findReciprocal(BigDecimal exchangeRate) {
        return BigDecimal.ONE.divide(exchangeRate, 12, RoundingMode.HALF_UP);
    }
}
