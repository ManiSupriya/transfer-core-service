package com.mashreq.transfercoreservice.notification.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Parameters to send email request.
 *
 */
@Getter @Setter
public class EmailRequest {

    private String fromEmailName;
    private String fromEmailAddress;
    private String subject;
    private String text;
    private String toEmailAddress;

}