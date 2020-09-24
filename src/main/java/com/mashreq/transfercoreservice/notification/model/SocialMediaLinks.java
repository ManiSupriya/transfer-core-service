package com.mashreq.transfercoreservice.notification.model;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialMediaLinks {


    private String socialMediaName;
    private String socialMediaLink;
    private String channelIdentifier;
}
