package com.mashreq.transfercoreservice.cardlesscash.constants;

/**
 * Interface to hold constant detail.
 */
public interface CardLessCashConstants {

    interface URL{
    	String CARD_LESS_CASH_BASE_URL = "/v1/cardless-cash";
        String CLC_BLOCK_URL="/request-block";
        String CLC_REQUEST_URL="/request";
        String CLC_QUERY_URL="/query/{accountNumber}";
    }
}
