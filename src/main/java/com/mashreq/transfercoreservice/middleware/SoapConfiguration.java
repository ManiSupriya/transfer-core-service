package com.mashreq.transfercoreservice.middleware;

import com.mashreq.mobcommons.services.middleware.MobSoapClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

@Configuration
public class SoapConfiguration {

    @Value("${esb-service.url}")
    private String cdmUrl;

    public static final String BINDINGS_PACKAGE = "com.mashreq.esbcore.bindings";
    @Autowired
    private Environment env;

    @Autowired
    private SoapServiceProperties serviceProperties;

    @Autowired
    MobSoapClientInterceptor logHttpHeaderClientInterceptor;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(BINDINGS_PACKAGE);
        return marshaller;
    }

    @Bean
    public WebServiceTemplate template() {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(marshaller());
        template.setUnmarshaller(marshaller());
        template.setDefaultUri(cdmUrl);
        return template;
    }

    /**
     * Bean for client that connects to ESB core
     *
     * @param marshaller marshaller for marshalling and unmarshalling messages
     * @return client for integration with ESB core
     */

    @Bean
    public WebServiceClient webServiceClient(Jaxb2Marshaller marshaller) {
        WebServiceClient serviceClient = new WebServiceClient(serviceProperties);
        serviceClient.setDefaultUri(cdmUrl);
        serviceClient.setMarshaller(marshaller);
        serviceClient.setUnmarshaller(marshaller);
        isClientInterceptorActive(serviceClient);
        return serviceClient;
    }

    /**
     * Adding client interceptor to WebServiceGatewaySupport if the profile is dev
     */
    private void isClientInterceptorActive(WebServiceGatewaySupport serviceClient) {
        ClientInterceptor[] interceptors = new ClientInterceptor[]{logHttpHeaderClientInterceptor};
        serviceClient.setInterceptors(interceptors);
    }
}
