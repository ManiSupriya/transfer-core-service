package com.mashreq.transfercoreservice.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialMediaLinks {
    private String socialMediaName;
    private String socialMediaLink;
    private String channelIdentifier;
}
