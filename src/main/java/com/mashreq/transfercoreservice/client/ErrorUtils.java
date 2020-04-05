package com.mashreq.transfercoreservice.client;

import com.mashreq.transfercoreservice.config.feign.FeignResponse;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author shahbazkh
 * @date 4/1/20
 */
public class ErrorUtils {

    public static boolean hasError(FeignResponse feignResponse) {
        return isNotBlank(feignResponse.getErrorId()) || isNotBlank(feignResponse.getErrorCode());
    }

    public static String getErrorDetails(FeignResponse feignResponse) {
        if (StringUtils.isNotBlank(feignResponse.getErrorDetails())) {
            return feignResponse.getErrorCode() + "," + feignResponse.getErrorDetails();
        }
        return feignResponse.getErrorCode();
    }
}
