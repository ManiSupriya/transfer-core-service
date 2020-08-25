package com.mashreq.transfercoreservice.config.feign;

import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.webcore.constants.WebConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Slf4j
@RequiredArgsConstructor
public class FeignAccessTokenInterceptor implements RequestInterceptor {

    @Autowired
    private SoapServiceProperties soapServiceProperties;

    /**
     *
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {

        requestTemplate.header(WebConstants.Headers.CHANNEL, soapServiceProperties.getUserId());
        requestTemplate.header(WebConstants.Headers.AUTHORIZATION, WebConstants.Headers.BEARER + " " + soapServiceProperties.getAccessToken());
    }

}
