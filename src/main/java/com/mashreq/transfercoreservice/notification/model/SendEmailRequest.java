package com.mashreq.transfercoreservice.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@Builder
public class SendEmailRequest {

    private String fromEmailAddress;
    private String fromEmailName;
    private String toEmailAddress;
    private String subject;
    private String templateName;
    private Map<String,String> templateKeyValues;
    private boolean isEmailPresent;
}
