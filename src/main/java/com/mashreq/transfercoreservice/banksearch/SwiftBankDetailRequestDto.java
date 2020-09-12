package com.mashreq.transfercoreservice.banksearch;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * @author shahbazkh
 * @date 3/23/20
 */

@Data
public class SwiftBankDetailRequestDto {

    @NotEmpty
    @Size(min = 8)
    private String swiftCode;
}
