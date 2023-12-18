package com.mashreq.transfercoreservice.config.feign;

import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.webcore.constants.WebConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import static com.mashreq.transfercoreservice.client.RequestMetadataMapper.FEIGN;
import static com.mashreq.transfercoreservice.client.RequestMetadataMapper.ORIGIN;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

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
        if(!checkAsyncCalls(requestTemplate)) {
            ServletRequestAttributes receivedRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (receivedRequestAttributes != null) {
                HttpServletRequest receivedRequest = receivedRequestAttributes.getRequest();
                for (Enumeration<?> e = receivedRequest.getHeaderNames(); e.hasMoreElements(); ) {
                    String nextHeaderName = (String) e.nextElement();
                    String headerValue = receivedRequest.getHeader(nextHeaderName);
                    requestTemplate.header(nextHeaderName, headerValue);
                }
            }
        }
        requestTemplate.header(WebConstants.Headers.CHANNEL, soapServiceProperties.getUserId());
        requestTemplate.header(WebConstants.Headers.AUTHORIZATION, WebConstants.Headers.BEARER + " " + soapServiceProperties.getAccessToken());
    }

    private boolean checkAsyncCalls(RequestTemplate requestTemplate){
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Collection<String> originValues = headers.get(ORIGIN);
        return isNotEmpty(originValues) && originValues.contains(FEIGN);
    }
}
