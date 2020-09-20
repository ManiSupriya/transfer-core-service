package com.mashreq.transfercoreservice.notification.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EmailTemplateParameters {

    private ChannelDetails channelIdentifier;
    private Map<String, String> socialMediaLinks;
    private EmailTemplateContactWebsiteContent htmlContactContents;

}
