package com.mashreq.transfercoreservice.http;

/**
 * @author shahbazkh
 * @date 5/20/20
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A filter which logs web requests that lead to an error in the system.
 */

@Slf4j
@Component
public class LogRequestFilter extends OncePerRequestFilter implements Ordered {

//    private int order = Ordered.LOWEST_PRECEDENCE - 8;

    private int order = Ordered.HIGHEST_PRECEDENCE;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        filterChain.doFilter(wrappedRequest, response);
        Map<String, Object> trace = getTrace(wrappedRequest);
        getBody(wrappedRequest, trace);
        logTrace(wrappedRequest, trace);
    }

    private void getBody(ContentCachingRequestWrapper request, Map<String, Object> trace) {
        // wrap request to make sure we can read the body of the request (otherwise it will be consumed by the actual
        // request handler)
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }

                trace.put("body", payload);
            }
        }
    }

    private void logTrace(HttpServletRequest request, Map<String, Object> trace) {

        Object method = trace.get("method");
        Object path = trace.get("path");
        Object statusCode = trace.get("statusCode");

        log.info(String.format("%s %s produced an error status code '%s'. Trace: '%s'", method, path, statusCode,
                trace));
    }

    protected Map<String, Object> getTrace(HttpServletRequest request) {
        Map<String, Object> trace = new LinkedHashMap<String, Object>();
        trace.put("method", request.getMethod());
        trace.put("path", request.getRequestURI());
        trace.put("query", request.getQueryString());

        return trace;
    }

}