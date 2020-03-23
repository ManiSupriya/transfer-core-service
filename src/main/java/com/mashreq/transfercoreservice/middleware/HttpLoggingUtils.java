package com.mashreq.transfercoreservice.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Logging web service Headers and Payload in Async call.
 *
 * To add a DB call in the future to log all the SOAP Message Details
 *
 */
@Component
public class HttpLoggingUtils{

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLoggingUtils.class);

    public void logMessage(String id, WebServiceMessage message){

        try{
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            message.writeTo(buffer);
            String soapPayload = buffer.toString(StandardCharsets.UTF_8.name());
            LOGGER.info( " #### {}  #### => {}",id, soapPayload);

        }catch(Exception e){
            LOGGER.warn("Not able to log http message");
        }
    }
}
