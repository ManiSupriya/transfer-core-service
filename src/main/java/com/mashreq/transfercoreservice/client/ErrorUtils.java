package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.config.feign.FeignResponse;
import com.mashreq.webcore.dto.response.Response;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shahbazkh
 * @date 4/1/20
 */
public class ErrorUtils {


    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return getErrorId(response) + "," + response.getErrorDetails();
        }
        return getErrorId(response);
    }

    private static String getErrorId(Response response) {
        return StringUtils.isNotBlank(response.getErrorId())
                ? response.getErrorId()
                : response.getErrorCode();
    }
}
