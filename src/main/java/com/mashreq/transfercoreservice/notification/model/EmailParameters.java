package com.mashreq.transfercoreservice.notification.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Parameters to configure e-mail.
 */
@Getter @Setter
public class EmailParameters {


    private String callCenterNo;
    private String emailSubject;
    private String fromEmailName;
    private String fromEmailAddress;
    private String localFundTransfer;
    private String otherFundTransfer;
}
