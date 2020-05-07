package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shahbazkh
 * @date 5/5/20
 */
@Slf4j
public class FundTransferContext {

    public static class Constants {

        public static final String BENEFICIARY_FUND_CONTEXT_KEY = "beneficiary-dto";
        public static final String ACCOUNT_DETAILS_FUND_CONTEXT_KEY = "account-details-dto";
        public static final String CUSTOMER_DETAIL_FUND_CONTEXT_KEY = "customer-detail-dto";
        public static final String TRANSFER_AMOUNT_IN_SRC_CURRENCY_FUND_CONTEXT_KEY = "transfer-amount-in-src-currency";
        public static final String EXCHANGE_RATE_FUND_CONTEXT_KEY = "exchange-rate";
        public static final String FLEX_PRODUCT_CODE_CONTEXT_KEY = "flex-product-code";

    }

    private Map<String, Object> map;

    public FundTransferContext() {
        map = new HashMap<>();
    }

    public void add(String key, Object value) {
        map.put(key, value);
    }

    public <T> T get(String key, Class<T> clazz) {
        if (!map.containsKey(key)) {
            log.error("Cannot find any data with key [ {} ] in Validation Context ", key);
        }
        return clazz.cast(map.get(key));
    }
}
