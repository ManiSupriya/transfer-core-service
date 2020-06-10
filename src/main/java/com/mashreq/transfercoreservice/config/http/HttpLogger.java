package com.mashreq.transfercoreservice.config.http;

/**
 * @author shahbazkh
 * @date 5/20/20
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mashreq.transfercoreservice.common.HeaderNames.X_CHANNEL_TRACE_ID;
import static java.time.Duration.between;

/**
 * A filter which logs web requests that lead to an error in the system.
 */

@Slf4j
@Component
@ConditionalOnProperty(value = "mob.http.detailed.logs.enabled", havingValue = "true", matchIfMissing = true)
public class HttpLogger extends OncePerRequestFilter implements Ordered {

    @Value("${mob.http.detailed.logs.request:true}")
    private String logRequest;

    @Value("${mob.http.detailed.logs.response:true}")
    private String logResponse;

    private int order = Ordered.HIGHEST_PRECEDENCE;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Instant startTime = Instant.now();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        Map<String, Object> trace = getTrace(wrappedRequest);

        if (Boolean.valueOf(logRequest)) {
            getRequestBody(wrappedRequest, trace);
        }

        if (Boolean.valueOf(logResponse)) {
            getResponseBody(wrappedResponse, trace);
        }
        logTrace(trace, between(startTime, Instant.now()).toMillis());

        //Copy Content of response back into original response
        wrappedResponse.copyBodyToResponse();
    }

    private void getRequestBody(ContentCachingRequestWrapper request, Map<String, Object> trace) {
        ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (requestWrapper != null) {
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, requestWrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }

                trace.put("request-body", payload);
            }
        }
    }

    private void getResponseBody(ContentCachingResponseWrapper response, Map<String, Object> trace) {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            byte[] buf = responseWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, responseWrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }

                trace.put("response-body", payload);
                trace.put("response-trace-header", responseWrapper.getHeader(X_CHANNEL_TRACE_ID));
            }
        }
    }

    private void logTrace(Map<String, Object> trace, long totalTimeTaken) {
        String httpLogSummary = trace.entrySet().stream()
                .map(s -> s.getKey() + " = " + s.getValue() + System.lineSeparator())
                .collect(Collectors.joining());
        log.info("HTTP REQUEST/RESPONSE SUMMARY \ntime-taken {} ms \n{}", totalTimeTaken, httpLogSummary);
    }

    protected Map<String, Object> getTrace(HttpServletRequest request) {
        Map<String, Object> trace = new LinkedHashMap<String, Object>();
        trace.put("method", request.getMethod());
        trace.put("path", request.getRequestURI());
        trace.put("query", request.getQueryString());
        trace.put("header", getHeaderNames(request));
        return trace;
    }

    private String getHeaderNames(HttpServletRequest request) {
        StringBuilder headerNames = new StringBuilder();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            final String headerName = headers.nextElement();
            headerNames.append(headerName);
            headerNames.append(":");
            headerNames.append(request.getHeader(headerName));
            headerNames.append(" ");
        }

        return headerNames.toString();
    }

}