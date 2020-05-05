package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 5/5/20
 */

@Data
@AllArgsConstructor
public class RoutingCode {

    private String type;
    private String value;
}
