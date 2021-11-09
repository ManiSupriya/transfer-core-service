package com.mashreq.transfercoreservice.util;

import com.mashreq.transfercoreservice.client.dto.CoreCurrencyConversionRequestDto;
import com.mashreq.transfercoreservice.client.dto.CurrencyConversionDto;
import com.mashreq.transfercoreservice.common.CommonUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

@RunWith(MockitoJUnitRunner.class)
public class CommonUtilsTest {

    @Test
    public void testExchangeRateTxt(){
        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(new BigDecimal(10));
        currencyConversionDto.setTransactionAmount(new BigDecimal(191));

        CoreCurrencyConversionRequestDto conversionRequestDto = new CoreCurrencyConversionRequestDto();
        conversionRequestDto.setTransactionCurrency("INR");
        conversionRequestDto.setAccountCurrency("AED");

        String exchangeRateTxt = CommonUtils.generateDisplayString(currencyConversionDto,conversionRequestDto);
        Assertions.assertNotNull(exchangeRateTxt);
        Assertions.assertEquals("1 AED = 19.10000 INR",exchangeRateTxt);

    }

    @Test
    public void testExchangeRateReciprocalTxt(){
        CurrencyConversionDto currencyConversionDto = new CurrencyConversionDto();
        currencyConversionDto.setAccountCurrencyAmount(new BigDecimal(36.5));
        currencyConversionDto.setTransactionAmount(new BigDecimal(10));

        CoreCurrencyConversionRequestDto conversionRequestDto = new CoreCurrencyConversionRequestDto();
        conversionRequestDto.setTransactionCurrency("USD");
        conversionRequestDto.setAccountCurrency("AED");

        String exchangeRateTxt = CommonUtils.generateDisplayString(currencyConversionDto,conversionRequestDto);
        Assertions.assertNotNull(exchangeRateTxt);
        Assertions.assertEquals("1 USD = 3.65000 AED",exchangeRateTxt);

    }

}
