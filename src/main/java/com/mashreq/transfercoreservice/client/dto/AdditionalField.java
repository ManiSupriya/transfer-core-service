package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
public class AdditionalField {
    private String fieldName;
    private boolean isRequired;
}