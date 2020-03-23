package com.mashreq.transfercoreservice.banksearch;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Data
public class BankDetailRequestDto {
    
    @NotEmpty
    private String type;

    @NotEmpty
    private String value;
}
