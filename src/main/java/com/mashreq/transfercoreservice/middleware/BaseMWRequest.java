package com.mashreq.transfercoreservice.middleware;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author shahbazkh
 * @date 2/25/20
 */

@Getter
@RequiredArgsConstructor
public class BaseMWRequest {
    /**
     * Service Type required for making Middleware Calls
     */
    private final String esbServiceType;

    /**
     * Service Code required for making Middleware Calls
     */
    private final String esbServiceCode;

    /**
     *
     */
    private final String srcMessageId;


}
