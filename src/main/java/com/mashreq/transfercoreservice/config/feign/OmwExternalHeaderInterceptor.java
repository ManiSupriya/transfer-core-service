package com.mashreq.transfercoreservice.config.feign;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Tracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.google.common.net.HttpHeaders.X_REQUEST_ID;
import static com.mashreq.transfercoreservice.common.CommonConstants.*;
import static com.mashreq.transfercoreservice.common.HtmlEscapeCache.htmlEscape;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.OMW_EXTERNAL_CALL_FAILED;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * Created by KrishnaKo on 05/01/2024
 */
@Slf4j
@AllArgsConstructor
public class OmwExternalHeaderInterceptor implements RequestInterceptor {

    private OmwExternalConfigProperties omwExternalConfigProperties;
    private final Tracer tracer;
    private RestTemplate restTemplate;
    private AccessTokenResponse accessTokenResponse;

    @Override
    public void apply(RequestTemplate requestTemplate) {


        addAuthorizationHeader(requestTemplate);
    }

    private String generateToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_MSG_ID, requireNonNull(tracer.currentTraceContext().context()).traceId());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(prepareTokenRequestString(), headers);
        log.info("Request sent for External Gateway access token");
        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(omwExternalConfigProperties.getUrl() + omwExternalConfigProperties.getTokenUrl(),
                HttpMethod.POST, entity, AccessTokenResponse.class);

        AccessTokenResponse accessTokenResponseDto;
        if (response.getStatusCode() == HttpStatus.OK && null != response.getBody()) {
            accessTokenResponseDto = requireNonNull(response).getBody();
            log.info("Response received for  access token {} : ",
                    htmlEscape(requireNonNull(accessTokenResponseDto).toString()));
            if (isNotEmpty(requireNonNull(accessTokenResponseDto).getAccessToken())) {
                return accessTokenResponseDto.getAccessToken();
            }
        }
        log.error("Not able to fetch External Gateway access token from api connect {}",
               response.getStatusCode());
        GenericExceptionHandler.handleError(OMW_EXTERNAL_CALL_FAILED,OMW_EXTERNAL_CALL_FAILED.getErrorMessage());
        return null;
    }

    private String prepareTokenRequestString() {
        return API_CONNECT_TOKEN_GRANT_TYPE_KEY + "=" +
                omwExternalConfigProperties.getGrantType() + "&" + API_CONNECT_SCOPE_KEY + "=" +
                omwExternalConfigProperties.getScope() + "&" + API_CONNECT_TOKEN_CLIENT_ID_KEY + "=" +
                omwExternalConfigProperties.getClientId() + "&" + API_CONNECT_TOKEN_CLIENT_SECRET_KEY +
                "=" + omwExternalConfigProperties.getClientSecret();
    }

    private void addAuthorizationHeader(RequestTemplate requestTemplate) {
        requestTemplate.header(AUTHORIZATION, BEARER + generateToken());
        requestTemplate.header(CONTENT_TYPE, APPLICATION_JSON);
        requestTemplate.header(X_REQUEST_ID, UUID.randomUUID().toString());
        requestTemplate.header(OVERLAY_CLIENT_ID_KEY, omwExternalConfigProperties.getClientId());
    }
}
