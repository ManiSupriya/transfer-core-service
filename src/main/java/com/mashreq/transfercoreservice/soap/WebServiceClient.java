package com.mashreq.transfercoreservice.soap;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;


public class WebServiceClient extends WebServiceGatewaySupport {

    /**
     * Contructor for setting environment parameters
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
        return getWebServiceTemplate().marshalSendAndReceive(getDefaultUri(), requestPayload);
    }

}
