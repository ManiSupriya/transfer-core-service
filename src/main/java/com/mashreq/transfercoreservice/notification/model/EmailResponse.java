package com.mashreq.transfercoreservice.notification.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Parameters received if email is successfully delivered
 *
 * @author pallaviG
 */

@Getter @Setter
public class EmailResponse {

    private String toEmailAddress;
    private String fromEmailAddress;
    private boolean success;

}