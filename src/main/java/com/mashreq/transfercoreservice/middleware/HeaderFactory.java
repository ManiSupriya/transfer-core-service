package com.mashreq.transfercoreservice.middleware;

import com.mashreq.esbcore.bindings.header.mbcdm.HeaderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderFactory {

    @Autowired
    private SoapServiceProperties soapServiceProperties;

    /*
    added current time instead of channleTraceId for srcMsgId as it channelTraceId >18 and was failing middleware calls
     */
    public HeaderType getHeader(String service, String msgId) {
        HeaderType header = new HeaderType();
        header.setSrcAppId(soapServiceProperties.getAppId());
        header.setOrgId(soapServiceProperties.getOriginId());
        header.setUserId(soapServiceProperties.getUserId());
        header.setSrcMsgId(msgId);
        header.setSrcAppTimestamp(getCurrentTimeStamp());
        header.setSrvCode(service);
        return header;
    }

    private XMLGregorianCalendar getCurrentTimeStamp() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(LocalDateTime.now().toString());
        } catch (Exception e) {
            log.error("Error setting time stamp in request {}", e);
            return null;
        }
    }
}
