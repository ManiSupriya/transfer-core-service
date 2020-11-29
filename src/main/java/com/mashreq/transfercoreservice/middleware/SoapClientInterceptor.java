package com.mashreq.transfercoreservice.middleware;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SoapClientInterceptor implements ClientInterceptor {

    @Autowired
    HttpLoggingUtils httpLoggingUtils;
    
    SoapClientInterceptor(HttpLoggingUtils httpLoggingUtils){
    	 this.httpLoggingUtils = httpLoggingUtils;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        httpLoggingUtils.logMessage("Client request message : ", messageContext.getRequest());
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        httpLoggingUtils.logMessage("Client response message : ", messageContext.getResponse());
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        try{
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            messageContext.getResponse().writeTo(buffer);
            String soapPayload = buffer.toString(StandardCharsets.UTF_8.name());
            log.error(" ### SOAP Message Fail ###", soapPayload);
        }catch (Exception e){
            log.error("### SOAP Message Fail ###", e);
        }

        return false;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception e) {
        // Not Implemented
    }
}
