package com.mashreq.transfercoreservice.middleware;

import com.mashreq.ms.exceptions.GenericExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.*;


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
            GenericExceptionHandler.handleError(CONNECTION_TIMEOUT_MW, CONNECTION_TIMEOUT_MW.getErrorMessage());
        }catch (Exception e){
            log.error("Error occurred in middle ware {} ", e);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR_MW, EXTERNAL_SERVICE_ERROR_MW.getErrorMessage());
        }
        return result;
    }

}
