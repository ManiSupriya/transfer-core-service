package com.mashreq.transfercoreservice.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateContactWebsiteContent {

    private String segment;
    private String htmlContent;

}
