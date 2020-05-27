package com.mashreq.transfercoreservice.middleware;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.Objects;

public class SoapWebserviceClientFactory {

    public static SoapClient soapClient(SoapServiceProperties props, Class<?>[] bindingClasses) {

        if (Objects.isNull(props)) {
            throw new RuntimeException("Cannot create Soap Client, props is empty");
        }

        if (Objects.isNull(bindingClasses) || bindingClasses.length == 0) {
            throw new RuntimeException("Cannot create Soap Client, bindings classes are missing");
        }

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(bindingClasses);
        return new SoapClient(props, marshaller);
    }


}
