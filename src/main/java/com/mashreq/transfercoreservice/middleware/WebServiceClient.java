package com.mashreq.transfercoreservice.middleware;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import com.mashreq.transfercoreservice.errors.TransferErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.MW_CONNECTION_TIMEOUT;


@Slf4j
public class WebServiceClient extends WebServiceGatewaySupport {

    /**
     * Constructor for setting environment parameters
     *
     * @param appProperties with EsbInfo object that contains connection and read timeout
     */
    public WebServiceClient(SoapServiceProperties appProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(appProperties.getConnectTimeout());
        requestFactory.setReadTimeout(appProperties.getReadTimeout());
        setMessageSender(new ClientHttpRequestMessageSender(requestFactory));
    }

    public Object exchange(Object requestPayload) {

        Object result = null;
        try {
            result = getWebServiceTemplate().marshalSendAndReceive(getDefaultUri(), requestPayload);
        } catch (WebServiceIOException e) {
            log.error("Cannot connect to middle ware {} ", e);
            GenericExceptionHandler.handleError(MW_CONNECTION_TIMEOUT, MW_CONNECTION_TIMEOUT.getErrorMessage());
        }
        return result;
    }

}
