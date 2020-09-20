package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

/**
 * Parameters to sent in send email request
 *
 * @author pallaviG
 */

@Data
public class EmailRequest {

    private String fromEmailName;
    private String fromEmailAddress;
    private String subject;
    private String text;
    private String toEmailAddress;

}