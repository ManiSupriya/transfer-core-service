package com.mashreq.transfercoreservice.config.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.mashreq.transfercoreservice.common.HeaderNames.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author shahbazkh
 * @date 2/27/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelTraceInterceptor implements HandlerInterceptor {

    private final ChannelTracerGenerator channelTracerGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String cifId = isBlank(request.getHeader(CIF_HEADER_NAME))
                ? "XXXXXXXXX"
                : request.getHeader(CIF_HEADER_NAME);

        final String xChannelTraceId = channelTracerGenerator.channelTraceId(
                request.getHeader(CHANNEL_TYPE_HEADER_NAME),
                cifId,
                request.getHeader(COUNTRY_HEADER_NAME));

        request.setAttribute(X_CHANNEL_TRACE_ID, xChannelTraceId);

        MDC.put(X_CHANNEL_TRACE_ID, xChannelTraceId);
        response.setHeader(X_CHANNEL_TRACE_ID, String.valueOf(request.getAttribute(X_CHANNEL_TRACE_ID)));
        return true;
    }



}
