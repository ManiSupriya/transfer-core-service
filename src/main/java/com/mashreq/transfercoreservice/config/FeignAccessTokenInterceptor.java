package com.mashreq.transfercoreservice.config;

import com.mashreq.webcore.constants.WebConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Slf4j
@RequiredArgsConstructor
public class FeignAccessTokenInterceptor implements RequestInterceptor {

    /**
     * Temporary
     * Set header to pass ldap token to other services
     * retrieve token from in memory
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {

        //TODO: Remove this hardcode Token
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJVQlRlc3Q0QG1hc2hyZXFkZXYuY29tIiwiY2hhbm5lbCI6IjJBRUMzNjRGQTg2MUFBMDY3QkYzOTI2QjZBMUQzMTNBIiwiZW1wbG95ZWVJZCI6bnVsbCwiYnJhbmNoIjoiQUQyQTU2QUVCN0FCRjM1QjYwNEZBODI1NzdEMTVCQUEiLCJhdXRob3JpdGllcyI6WyJVYl9Vc2VyIiwiVUJfUE9XRVIiLCJSZXBvcnRpbmdHcm91cCB7ODJkYWU4MDItNmY2Ni00YTBjLTg5ODAtZDA0ZTRhNDE5OWRlfSIsIlJlcG9ydGluZ0dyb3VwIHs4NzQ0YzU2YS01YWI5LTRhODMtOTk2OS0yZWQ1ZjIxMDViYjN9IiwiVUJVQVQiXSwiZW5hYmxlZCI6dHJ1ZSwiY2xpZW50X2lkIjoiYnJhbmNocG9ydGFsIiwiYnJhbmNoQ29kZSI6IkM3MkI0RDU0OTBFNjIwRTg5NUVEQUFDNzBBOEU5NUMzIiwibmJmIjoxNTgyMjA5NjMxLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiLCJpZGVudGl0eSJdLCJuYW1lIjoiNjYxOERGNENCMjhGMjNGMjNGMzI3M0NFQzZGQ0YyMjMiLCJleHAiOjE1ODIyMTY4MzEsImp0aSI6IjAxYTA3YTZjLWQzMDItNDhmMC1iNDQ2LTQwY2I0MTBlY2FhZSIsImVtYWlsIjpudWxsfQ.hnrskgfb8DuP26VVVrrFcNVRXPk3TKSjslzImlw_dtM";
        requestTemplate.header(WebConstants.Headers.CHANNEL, "UBOUSER");
        requestTemplate.header(WebConstants.Headers.AUTHORIZATION, WebConstants.Headers.BEARER + " " + accessToken);
    }

}
