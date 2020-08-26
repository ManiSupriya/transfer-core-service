package com.mashreq.transfercoreservice.notification.model;

import lombok.Data;

/**
 * POJO to store response of SMS service
 *
 * @author Kalim
 */

@Data
public class SMSResponse {

    private String statusCode;
    private String statusDescription;

}