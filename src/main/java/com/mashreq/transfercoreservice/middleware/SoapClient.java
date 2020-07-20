package com.mashreq.transfercoreservice.middleware;

import com.mashreq.mobcommons.services.middleware.MobSoapClientInterceptor;
import com.mashreq.mobcommons.services.middleware.MobSoapLogger;
import com.mashreq.ms.exceptions.GenericExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CONNECTION_TIMEOUT_MW;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR_MW;

@Slf4j
public class SoapClient extends WebServiceGatewaySupport {

    protected SoapClient(SoapServiceProperties soapProperties, Jaxb2Marshaller marshaller) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(soapProperties.getConnectTimeout());
        requestFactory.setReadTimeout(soapProperties.getReadTimeout());
        setMessageSender(new ClientHttpRequestMessageSender(requestFactory));
        ClientInterceptor[] interceptors = new ClientInterceptor[]{new MobSoapClientInterceptor( new MobSoapLogger())};

        this.setDefaultUri(soapProperties.getUrl());
        this.setMarshaller(marshaller);
        this.setUnmarshaller(marshaller);
        this.setInterceptors(interceptors);
    }

    public Object exchange(Object requestPayload) {

        Object result = null;
        try {
            result = getWebServiceTemplate().marshalSendAndReceive(getDefaultUri(), requestPayload);
        } catch (WebServiceIOException e) {
            log.error("Cannot connect to middle ware {} ", e);
            GenericExceptionHandler.handleError(CONNECTION_TIMEOUT_MW, CONNECTION_TIMEOUT_MW.getErrorMessage());
        } catch (Exception e) {
            log.error("Error occurred in middle ware {} ", e);
            GenericExceptionHandler.handleError(EXTERNAL_SERVICE_ERROR_MW, EXTERNAL_SERVICE_ERROR_MW.getErrorMessage());
        }
        return result;
    }
}
