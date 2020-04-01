package com.mashreq.transfercoreservice.config;

import com.mashreq.webcore.dto.response.Response;
import lombok.Data;
import lombok.ToString;

/**
 * @author shahbazkh
 * @date 4/1/20
 */
@Data
@ToString(callSuper = true)
public class FeignResponse<T> extends Response<T> {

    private String hasError;
    private String errorId;
    private String errorDetails;
    private String uriPath;
    private String errorMessage;
}
