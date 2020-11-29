package com.mashreq.transfercoreservice.middleware;

import static com.mashreq.transfercoreservice.errors.TransferErrorCode.CONNECTION_TIMEOUT_MW;
import static com.mashreq.transfercoreservice.errors.TransferErrorCode.EXTERNAL_SERVICE_ERROR_MW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;
import com.mashreq.esbcore.middleware.MobSoapServiceProperties;
import com.mashreq.ms.exceptions.GenericExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoapClient extends WebServiceGatewaySupport {
	
	@Autowired
    SoapClientInterceptor logHttpHeaderClientInterceptor;

    protected SoapClient(SoapServiceProperties soapProperties, Jaxb2Marshaller marshaller) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(soapProperties.getConnectTimeout());
        requestFactory.setReadTimeout(soapProperties.getReadTimeout());
        setMessageSender(new ClientHttpRequestMessageSender(requestFactory));
        ClientInterceptor[] interceptors = new ClientInterceptor[]{logHttpHeaderClientInterceptor};

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
