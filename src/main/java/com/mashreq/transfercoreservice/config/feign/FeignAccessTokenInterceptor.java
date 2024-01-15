package com.mashreq.transfercoreservice.config.feign;

import com.mashreq.transfercoreservice.middleware.SoapServiceProperties;
import com.mashreq.webcore.constants.WebConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static com.mashreq.ms.commons.cache.HeaderNames.*;
import static com.mashreq.transfercoreservice.client.RequestMetadataMapper.FEIGN;
import static com.mashreq.transfercoreservice.client.RequestMetadataMapper.ORIGIN;
import static com.mashreq.transfercoreservice.common.CommonConstants.CLIENT_ID_HEADER;
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
    private final List<String> ALL_HEADERS =
            List.of(CIF_HEADER_NAME, CHANNEL_TYPE_HEADER_NAME, COUNTRY_HEADER_NAME,
                    USSM_DATE_FORMAT, X_USSM_USER_LOGIN_ID, X_USSM_USER_REDIS_KEY,
                    X_USSM_PRIMARY_ACCOUNT, X_USSM_ACCOUNTS, X_USSM_SEGMENT, X_USSM_USER_TYPE,
                    X_USSM_USER_TWO_FA_AUTHDATE, X_USSM_USER_MOBILE_NUMBER, X_USSM_EMAIL_ID,
                    X_USSM_CARDS, X_USSM_USER_NAME, X_CORRELATION_ID, X_USSM_USER_SUSPENDED_TXS,
                    X_USSM_USER_DEVICE_IP, X_USSM_USER_REGION, X_USSM_USER_IAM_ID, CLIENT_ID_HEADER);

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
                    if (ALL_HEADERS.contains(nextHeaderName.toUpperCase())) {
                        requestTemplate.header(nextHeaderName, headerValue);
                    }
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
