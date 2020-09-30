package com.mashreq.transfercoreservice.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@Builder
public class EmailTemplateParameters {

    private ChannelDetails channelIdentifier;
    private Map<String, String> socialMediaLinks;
    private EmailTemplateContactWebsiteContent htmlContactContents;

}
