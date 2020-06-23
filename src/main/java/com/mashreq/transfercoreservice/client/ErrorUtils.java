package com.mashreq.transfercoreservice.client;

import com.mashreq.webcore.dto.response.Response;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shahbazkh
 * @date 4/1/20
 */
public class ErrorUtils {


    public static String getErrorDetails(Response response) {
        if (StringUtils.isNotBlank(response.getErrorDetails())) {
            return response.getErrorCode() + "," + response.getErrorDetails();
        }
        return response.getErrorCode();
    }

}
