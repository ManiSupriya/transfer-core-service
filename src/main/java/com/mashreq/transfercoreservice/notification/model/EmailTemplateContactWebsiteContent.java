package com.mashreq.transfercoreservice.notification.model;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateContactWebsiteContent {

    private String segment;
    private String htmlContent;

}
