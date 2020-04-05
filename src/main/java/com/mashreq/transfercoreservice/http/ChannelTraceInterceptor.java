package com.mashreq.transfercoreservice.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author shahbazkh
 * @date 2/27/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelTraceInterceptor implements HandlerInterceptor {

    public static final String X_CHANNEL_TRACE_ID = "X-CHANNEL-TRACE-ID";
    public static final String USER_AGENT_KEY = "user-agent";
    public static final String CIF_KEY = "X-CIF-ID";
    private final ChannelTracerGenerator channelTracerGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("URI : {}, USER-AGENT : {}, CIF {}", request.getRequestURI(), request.getHeader(USER_AGENT_KEY), request.getHeader(CIF_KEY), request.getRequestURI());

        final String cifId = StringUtils.isBlank(request.getHeader(CIF_KEY)) ? "XXXXXXXXX" : request.getHeader(CIF_KEY);

        final String xChannelTraceId = channelTracerGenerator.channelTraceId(request.getHeader(USER_AGENT_KEY), cifId);
        request.setAttribute(X_CHANNEL_TRACE_ID, xChannelTraceId);
        request.setAttribute("X-CHANNEL-HOST", request.getRemoteAddr());
        request.setAttribute("X-CHANNEL-NAME", getChannel(request.getHeader(USER_AGENT_KEY)));
        MDC.put(X_CHANNEL_TRACE_ID, xChannelTraceId);
        response.setHeader(X_CHANNEL_TRACE_ID, String.valueOf(request.getAttribute(X_CHANNEL_TRACE_ID)));
        return true;
    }

    private String getChannel(String userAgent) {
        if ("MOBILE".equalsIgnoreCase(userAgent)) {
            return "MOBILE";
        } else if ("WEB".equalsIgnoreCase(userAgent)) {
            return "WEB";
        } else {
            return "UNKNOWN";
        }
    }
}
