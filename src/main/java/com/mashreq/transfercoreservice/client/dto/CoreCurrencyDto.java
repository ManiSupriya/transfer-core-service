package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

/**
 * @author Thanigachalam P
 *
 */
@Data
public class CoreCurrencyDto {

    private String code;
    private String description;
    private String iso;
    private String function;
    private boolean quickRemitEnabled;
    private boolean swiftTransferEnabled;
}
