package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

/**
 * Parameters received if email is successfully delivered
 *
 * @author pallaviG
 */

@Data
public class EmailResponse {

    private String toEmailAddress;
    private String fromEmailAddress;
    private boolean success;

}