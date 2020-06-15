package com.mashreq.transfercoreservice.cardlesscash.constants;

/**
 * Interface to hold constant detail.
 */
public interface CardLessCashConstants {

    interface URL{

        String CLC_BLOCK_URL="/api/accounts/cardless-cash/request-block";
        String CLC_REQUEST_URL="/api/accounts/cardless-cash/request";
        String CLC_QUERY_URL="/api/accounts/cardless-cash/query/{ref-no}";
    }
}
