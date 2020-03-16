package com.mashreq.transfercoreservice.http;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.HEADER_MISSING_CIF;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author shahbazkh
 * @date 2/27/20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelTraceInterceptor implements HandlerInterceptor {

    public static final String X_CHANNEL_TRACE_ID = "X-CHANNEL-TRACE-ID";
    private final ChannelTracerGenerator channelTracerGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("URI : {}, USER-AGENT : {}, CIF {}", request.getRequestURI(), request.getHeader("user-agent"), request.getHeader("X-CIF-ID"), request.getRequestURI());

        if (isBlank(request.getHeader("X-CIF-ID")))
            GenericExceptionHandler.handleError(HEADER_MISSING_CIF, HEADER_MISSING_CIF.getErrorMessage());

        final String xChannelTraceId = channelTracerGenerator.channelTraceId(request.getHeader("user-agent"), request.getHeader("X-CIF-ID"));
        request.setAttribute(X_CHANNEL_TRACE_ID, xChannelTraceId);
        request.setAttribute("X-CHANNEL-HOST", request.getRemoteAddr());
        request.setAttribute("X-CHANNEL-NAME", getChannel(request.getHeader("user-agent")));
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
